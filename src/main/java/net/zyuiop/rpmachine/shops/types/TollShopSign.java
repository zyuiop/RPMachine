package net.zyuiop.rpmachine.shops.types;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.economy.Messages;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.json.JsonExclude;
import net.zyuiop.rpmachine.permissions.ShopPermissions;
import net.zyuiop.rpmachine.shops.ShopBuilder;
import net.zyuiop.rpmachine.utils.Symbols;
import org.apache.commons.lang.StringUtils;
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
import java.util.Optional;
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

        static Door of(Runnable open, Runnable close) {
            return new Door() {
                @Override
                public void open() {
                    open.run();
                }

                @Override
                public void close() {
                    close.run();
                }
            };
        }

        default Door merge(Door other) {
            if (other == null) return this;
            Door self = this;
            return of(() -> {
                self.open();
                other.open();
            }, () -> {
                self.close();
                other.close();
            });
        }
    }

    private Door getDoor(Block block) {
        // TODO: check door is in bounds
        BlockData data = block.getBlockData();
        if (data instanceof org.bukkit.block.data.type.Door || data instanceof Gate) {
            Openable door = (Openable) data;

            return Door.of(() -> {
                door.setOpen(true);
                block.setBlockData(door);
            }, () -> {
                door.setOpen(false);
                block.setBlockData(door);
            });
        } else if (data instanceof Fence) {
            Fence fence = (Fence) data;
            Set<BlockFace> faces = new HashSet<>(fence.getFaces());
            Material mat = block.getType();

            return Door.of(() -> block.setType(Material.AIR), () -> {
                block.setType(mat);
                Fence f = (Fence) block.getBlockData();
                for (BlockFace bf : f.getAllowedFaces())
                    f.setFace(bf, faces.contains(bf));
                block.setBlockData(f);
            });
        } else return null;
    }

    private TollShopDirection direction;
    @JsonExclude
    private Door door;

    private Door messagingDoor(Door underlying) {
        Sign sign = (Sign) getLocation().getBlock().getState();
        String arrow = TollShopSign.this.direction.arrow;

        return underlying.merge(Door.of(() -> {
            sign.setLine(0, "");
            sign.setLine(1, ChatColor.GREEN + "Bienvenue!");
            sign.setLine(2, ChatColor.GREEN + arrow + arrow + arrow + arrow);
            sign.setLine(3, "");

            sign.update();
        }, () -> {
            sign.setLine(0, ChatColor.GREEN + "Accès payant");
            sign.setLine(1, ChatColor.BLUE + "Prix : " + price);
            sign.setLine(2, owner().shortDisplayable());
            sign.setLine(3, ChatColor.RED + arrow);

            sign.update();
        }));
    }

    private Door detectDoor() throws ShopBuilder.SignPermissionError {
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

                // Found a door
                if (tmpDoor != null) {
                    City c = RPMachine.getInstance().getCitiesManager().getCityHere(use.getChunk());
                    if (c != null) {
                        Plot p = c.getPlotHere(use.getLocation());

                        if (
                                ((p == null || p.ownerTag() == null) && !c.tag().equals(ownerTag())) // In the city itself or unsold plot: do the sign belong to the city
                                        ||
                                (p != null && p.ownerTag() != null && !p.ownerTag().equalsIgnoreCase(ownerTag()))) // In a plot: do the sign belong to the plot owner
                        {
                            throw new ShopBuilder.SignPermissionError("La parcelle ne vous appartient pas");
                        }
                    }

                    door = tmpDoor.merge(door);
                }
            }

            if (door != null)
                return messagingDoor(door); // Add the message part

            current = current.getRelative(direction.getBlockX(), 0, direction.getBlockZ());
        }

        return null;
    }

    public TollShopSign(Location location) {
        super(location);
    }

    public TollShopSign() {
        super();
    }

    public void display() {
        Block block = location.getLocation().getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();

            if (this.door == null) {
                try {
                    this.door = detectDoor();

                    if (this.door == null) {
                        sign.setLine(0, ChatColor.RED + "Accès payant");
                        sign.setLine(1, ChatColor.RED + "Erreur!");
                        sign.setLine(2, ChatColor.RED + "Porte");
                        sign.setLine(3, ChatColor.RED + "introuvable.");
                    } else door.close();
                } catch (ShopBuilder.SignPermissionError error) {
                    sign.setLine(0, owner().shortDisplayable());
                    sign.setLine(1, ChatColor.RED + "Porte dans un");
                    sign.setLine(2, ChatColor.RED + "claim ou plot");
                    sign.setLine(3, ChatColor.RED + "innaccessible");
                }
            }

            Bukkit.getScheduler().runTask(RPMachine.getInstance(), (Runnable) sign::update);
        } else {
            Bukkit.getLogger().info("Error : sign is not a sign, at " + location.toString());
        }
    }

    void clickPrivileged(Player player, RoleToken tt, PlayerInteractEvent event) {
        display(); // try to regen door
        clickUser(player, event);
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


    public static class Builder extends ShopBuilder<TollShopSign> {
        @Override
        public void describeFormat(Player player) {
            player.sendMessage(ChatColor.YELLOW + " - TollShop");
            player.sendMessage(ChatColor.AQUA + " - Prix du passage");
            player.sendMessage(ChatColor.AQUA + " - Position de la porte (-> ou <-)");
        }

        @Override
        public boolean hasPermission(RoleToken player) {
            return player.hasDelegatedPermission(ShopPermissions.CREATE_TOLL_SHOPS);
        }

        @Override
        public void postCreateInstructions(Player player) {
            player.sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] " + ChatColor.GREEN + "Votre péage est prêt. Placez une porte, cloture ou similaire dans les 5 blocs à l'arrière du panneau dans la direction indiquée.");
        }

        @Override
        public Optional<TollShopSign> parseSign(Block block, RoleToken tt, String[] lines) throws SignPermissionError, SignParseError {
            return Optional.of(new TollShopSign(block.getLocation()))
                    .flatMap(sign -> extractDouble(lines[1]).map(price -> {
                        if (price > 100_000_000_000D)
                            throw new SignParseError("Le prix maximal est dépassé (100 milliards)");
                        sign.price = price;
                        return sign;
                    }))
                    .flatMap(sign -> Optional.ofNullable(lines[2]).map(direction -> {
                        if (!direction.equalsIgnoreCase("->") && !direction.equalsIgnoreCase("<-"))
                            throw new SignParseError("La direction est incorrecte (-> ou <- uniquement, ici " + direction + ")");
                        sign.direction = direction.equals("->") ? TollShopDirection.RIGHT : TollShopDirection.LEFT;
                        return sign;
                    }))
                    .map(sign -> {
                        sign.setOwner(tt.getTag());

                        return sign;
                    });
        }
    }

    @Override
    public void debug(Player p) {
        p.sendMessage(ChatColor.YELLOW + "-----[ Débug Shop ] -----");
        p.sendMessage(ChatColor.YELLOW + "Price : " + getPrice());
        p.sendMessage(ChatColor.YELLOW + "Owner (Tag/displayable) : " + ownerTag() + " / " + owner().displayable());
        p.sendMessage(ChatColor.YELLOW + "Direction : " + direction);
    }
}
