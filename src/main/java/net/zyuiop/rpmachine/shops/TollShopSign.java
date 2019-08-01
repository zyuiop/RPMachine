package net.zyuiop.rpmachine.shops;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.economy.Messages;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.json.JsonExclude;
import net.zyuiop.rpmachine.permissions.ShopPermissions;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class TollShopSign extends AbstractShopSign {
    public static enum TollShopDirection {
        LEFT(Math.PI / 2, Symbols.ARROW_LEFT), RIGHT(-Math.PI / 2, Symbols.ARROW_RIGHT);

        public final double angle;
        public final String arrow;

        TollShopDirection(double angle, String arrow) {
            this.angle = angle;
            this.arrow = arrow;
        }
    }

    public static interface Door {
        void open();

        void close();

        default Door merge(Door other) {
            Door self = this;
            return new Door() {
                @Override
                public void open() {
                    self.open();
                    other.open();
                }

                @Override
                public void close() {
                    self.close();
                    other.close();
                }
            };
        }
    }

    private Door getDoor(Block block) {
        // TODO: check door is in bounds
        BlockData data = block.getBlockData();
        if (data instanceof org.bukkit.block.data.type.Door || data instanceof Gate) {
            Openable door = (Openable) data;

            return new Door() {
                @Override
                public void open() {
                    door.setOpen(true);
                    block.setBlockData(door);
                }

                @Override
                public void close() {
                    door.setOpen(false);
                    block.setBlockData(door);
                }
            };
        } else if (data instanceof Fence) {
            Fence fence = (Fence) data;
            Set<BlockFace> faces = new HashSet<>(fence.getFaces());
            Material mat = block.getType();

            return new Door() {
                @Override
                public void open() {
                    block.setType(Material.AIR);
                }

                @Override
                public void close() {
                    block.setType(mat);
                    Fence fence = (Fence) block.getBlockData();
                    for (BlockFace bf : fence.getAllowedFaces())
                        fence.setFace(bf, faces.contains(bf));
                    block.setBlockData(fence);
                }
            };
        } else return null;
    }

    private TollShopDirection direction;
    @JsonExclude
    private Door door;

    private Door detectDoor() {
        // Get a zone of up to 5 blocks behind the sign
        Block block = getLocation().getBlock();
        WallSign s = (WallSign) block.getBlockData();
        Vector direction = s.getFacing().getOppositeFace().getDirection();

        boolean isOnGround = block.getRelative(0, -1, 0).getType() != Material.AIR;
        Vector rotated = direction.clone().rotateAroundY(this.direction.angle);

        Block current = block.getRelative(rotated.getBlockX(), isOnGround ? 0 : -1, rotated.getBlockZ());
        for (int i = 0; i < 5; ++i) {
            Door door = null;

            for (int y = 0; y < 2; ++y) {
                Block use = current.getRelative(0, y, 0);
                Door tmpDoor = getDoor(use);

                if (tmpDoor != null) {
                    if (door != null) {
                        door = door.merge(tmpDoor);
                    } else door = tmpDoor;
                }
            }

            if (door != null) {
                Sign sign = (Sign) block.getState();
                String arrow = TollShopSign.this.direction.arrow;
                return door.merge(new Door() {
                    @Override
                    public void open() {
                        sign.setLine(0, "");
                        sign.setLine(1, ChatColor.GREEN + "Bienvenue!");
                        sign.setLine(2, ChatColor.GREEN + arrow + arrow + arrow + arrow);
                        sign.setLine(3, "");

                        sign.update();
                    }

                    @Override
                    public void close() {
                        sign.setLine(0, ChatColor.GREEN + "Accès payant");
                        sign.setLine(1, ChatColor.BLUE + "Prix : " + price);
                        sign.setLine(2, owner().shortDisplayable());
                        sign.setLine(3, ChatColor.RED + arrow);

                        sign.update();
                    }
                });
            }

            current = current.getRelative(direction.getBlockX(), 0, direction.getBlockZ());
        }

        return null;
    }

    public TollShopSign(Location location, TollShopDirection direction, Double price) {
        super(location);
        this.direction = direction;
        this.price = price;
    }

    public TollShopSign() {
        super();
    }

    public void display() {
        Block block = location.getLocation().getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();

            if (this.door == null) {
                this.door = detectDoor();

                if (this.door == null) {
                    sign.setLine(0, ChatColor.RED + "Accès payant");
                    sign.setLine(1, ChatColor.RED + "Erreur!");
                    sign.setLine(2, ChatColor.RED + "Porte");
                    sign.setLine(3, ChatColor.RED + "introuvable.");
                    return;
                }

                door.close(); // Will display the sign as well
            }

            Bukkit.getScheduler().runTask(RPMachine.getInstance(), (Runnable) sign::update);
        } else {
            Bukkit.getLogger().info("Error : sign is not a sign, at " + location.toString());
        }
    }

    public void rightClick(Player player, PlayerInteractEvent event) {
        super.rightClick(player, event);
        RPMachine.getInstance().getShopsManager().save(this);
    }

    void clickPrivileged(Player player, RoleToken tt, PlayerInteractEvent event) {
        // Whatever
        if (door == null)
            display(); // try to regen door
        else clickUser(player, event);
    }

    void clickUser(Player player, PlayerInteractEvent event) {
        RoleToken token = RPMachine.getPlayerRoleToken(player);

        if (door == null) {
            player.sendMessage(ChatColor.RED + "La porte est introuvable.");
        } else {
            if (!token.checkDelegatedPermission(ShopPermissions.USE_TOLL))
                return;

            EconomyManager manager = RPMachine.getInstance().getEconomyManager();
            manager.transferMoneyBalanceCheck(token.getLegalEntity(), owner(), price, result -> {
                if (result) {
                    player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Le passage a bien été ouvert pour " + price + " " + EconomyManager.getMoneyName());
                    player.playSound(getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                    door.open();

                    Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(), () -> {
                        door.close();
                        player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.YELLOW + "Le passage s'est refermé.");
                    }, 100L);
                } else {
                    player.sendMessage(Messages.NOT_ENOUGH_MONEY.getMessage());
                }
            });
        }
    }
}
