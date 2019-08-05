package net.zyuiop.rpmachine.shops.types;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.entities.AdminLegalEntity;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.permissions.ShopPermissions;
import net.zyuiop.rpmachine.shops.ShopBuilder;
import net.zyuiop.rpmachine.shops.ShopsManager;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class XPShopSign extends AbstractShopSign {
    private ShopAction action;
    private int points;
    private int available;

    public XPShopSign(Location location) {
        super(location);
    }

    public int getAvailable() {
        return available;
    }

    public void display() {
        Block block = location.getLocation().getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();

            sign.setLine(0, owner().shortDisplayable());
            if (action == ShopAction.BUY) {
                sign.setLine(1, ChatColor.GREEN + "achète ");
            } else {
                sign.setLine(1, ChatColor.BLUE + "vend ");
            }
            sign.setLine(2, ChatColor.DARK_GREEN + "" + ChatColor.BOLD + points + " pts XP");
            sign.setLine(3, ChatColor.BLUE + "Prix : " + price);

            Bukkit.getScheduler().runTask(RPMachine.getInstance(), (Runnable) sign::update);
        } else {
            Bukkit.getLogger().info("Error : sign is not a sign, at " + location.toString());
        }
    }

    @Override
    public void breakSign() {
        if (!(owner() instanceof AdminLegalEntity)) {
            // TODO: find way to drop xp
        }

        super.breakSign();
    }

    void removeXP(Player player) {
        player.giveExp(-points);
    }

    void clickPrivileged(Player player, RoleToken tt, PlayerInteractEvent event) {
        if (owner() instanceof AdminLegalEntity) {
            clickUser(player, event);
            return;
        }

        if (action == ShopAction.SELL && event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getTotalExperience() > this.points) {
            available += 1;
            removeXP(event.getPlayer());

            event.getPlayer().sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.YELLOW + "Vous venez de placer " + ChatColor.DARK_GREEN + points + ChatColor.YELLOW + " XP dans le shop.");
        } else if (action == ShopAction.SELL) {
            event.getPlayer().sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.YELLOW + "Il y a actuellement " + ChatColor.GOLD + this.available + ChatColor.YELLOW + " * " + this.points + " XP dans ce shop.");
        } else {
            if (!tt.checkDelegatedPermission(ShopPermissions.GET_SHOP_STOCK))
                return;

            if (available > 0) {
                available--;

                player.giveExp(points);
                player.sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.GREEN + "Vous venez de récupérer " + points + " XP. Il reste " + available + " lots.");
            } else {
                player.sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.RED + "Pas assez d'expérience disponible.");
            }
        }
    }

    void clickUser(Player player, PlayerInteractEvent event) {
        RoleToken token = RPMachine.getPlayerRoleToken(player);

        if (action == ShopAction.BUY) {
            ItemStack click = event.getItem();
            if (player.getTotalExperience() >= points) {
                if (owner().transfer(price, token.getLegalEntity())) {
                    Messages.creditEntity(player, token.getLegalEntity(), price, "vente de " + points + " XP");
                    removeXP(event.getPlayer());
                    available += 1;
                } else {
                    player.sendMessage(ChatColor.RED + "L'acheteur n'a plus assez d'argent.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Vous n'avez pas assez d'expérience.");
            }
        } else if (action == ShopAction.SELL) {
            if (!token.checkDelegatedPermission(ShopPermissions.BUY_ITEMS))
                return;

            if (available < 1 && !(owner() instanceof AdminLegalEntity) /* Admin shop : unlimited resources */) {
                player.sendMessage(ChatColor.RED + "Il n'y a pas assez d'XP à vendre.");
                return;
            }

            if (token.getLegalEntity().withdrawMoney(price)) {
                creditToOwner();
                Messages.debitEntity(player, token.getLegalEntity(), price, "achat de " + points + " XP");
                available -= 1;
                player.giveExp(points);
            } else {
                Messages.notEnoughMoneyEntity(player, token.getLegalEntity(), price);
            }
        }
    }

    @Override
    public void debug(Player p) {
        p.sendMessage(ChatColor.YELLOW + "-----[ Débug Shop ] -----");
        p.sendMessage(ChatColor.YELLOW + "Price : " + getPrice());
        p.sendMessage(ChatColor.YELLOW + "Owner (Tag/displayable) : " + ownerTag() + " / " + owner().displayable());
        p.sendMessage(ChatColor.YELLOW + "Action : " + action);
        p.sendMessage(ChatColor.YELLOW + "Item : " + this.points + " XP");
        p.sendMessage(ChatColor.YELLOW + "Available packages : " + getAvailable());
    }

    @Override
    public String describe() {
        String typeLine = action == ShopAction.BUY ? net.md_5.bungee.api.ChatColor.RED + "Achat" : net.md_5.bungee.api.ChatColor.GREEN + "Vente";
        String size = (getAvailable() > 0 ? net.md_5.bungee.api.ChatColor.GREEN : net.md_5.bungee.api.ChatColor.RED) + "" + getAvailable() + " en stock";

        return super.describe() + typeLine + ChatColor.YELLOW + " de lots de " + this.points + " XP" +
                " pour " + ChatColor.AQUA + price + RPMachine.getCurrencyName() + ChatColor.YELLOW +
                " (" + size + ChatColor.YELLOW + ")";
    }

    public static class Builder extends ShopBuilder<XPShopSign> {
        @Override
        public void describeFormat(Player player) {
            player.sendMessage(ChatColor.YELLOW + " - XPShop");
            player.sendMessage(ChatColor.AQUA + " - Prix par lot");
            player.sendMessage(ChatColor.AQUA + " - Pts d'XP par lot");
            player.sendMessage(ChatColor.AQUA + " - Achat ou Vente");
        }

        @Override
        public boolean hasPermission(RoleToken player) {
            return player.getLegalEntity() instanceof PlayerData;
        }

        public void postCreateInstructions(Player player) {
            player.sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] " + ChatColor.GREEN + "Votre boutique est prête !");
        }

        @Override
        public Optional<XPShopSign> parseSign(Block block, RoleToken tt, String[] lines) throws SignPermissionError, SignParseError {
            return Optional.of(new XPShopSign(block.getLocation()))
                    .flatMap(sign -> extractPrice(lines[1]).map(price -> {
                        if (price > 100_000_000_000D)
                            throw new SignParseError("Le prix maximal est dépassé (100 milliards)");
                        sign.price = price;
                        return sign;
                    }))
                    .flatMap(sign -> extractInt(lines[2]).map(bundleSize -> {
                        if (bundleSize > 100)
                            throw new SignParseError("Impossible de vendre ou acheter des lots de plus de 100 XP.");
                        sign.points = bundleSize;
                        return sign;
                    }))
                    .flatMap(sign -> Optional.ofNullable(lines[3]).map(action -> {
                        if (action.equalsIgnoreCase("achat") || action.equalsIgnoreCase("buy")) {
                            if (!tt.hasDelegatedPermission(ShopPermissions.CREATE_BUY_SHOPS))
                                throw new SignPermissionError("Impossible de créer un shop de vente.");

                            sign.action = ShopAction.BUY;
                        } else if (action.equalsIgnoreCase("vente") || action.equalsIgnoreCase("sell")) {
                            if (!tt.hasDelegatedPermission(ShopPermissions.CREATE_SELL_SHOPS))
                                throw new SignPermissionError("Impossible de créer un shop de vente.");

                            sign.action = ShopAction.SELL;
                        } else throw new SignParseError("Type d'action non reconnu '" + action + "'");

                        return sign;
                    }))
                    .map(sign -> {
                        sign.setOwner(tt.getTag());
                        return sign;
                    });
        }
    }
}
