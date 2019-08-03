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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ItemShopSign extends AbstractShopSign {
    private ItemStackStorage itemType;
    private int amountPerPackage;
    private ShopAction action;
    private int available;
    public ItemShopSign(Location location) {
        super(location);
    }

    public int getAvailable() {
        return available;
    }

    public boolean isItemValid(ItemStack itemStack) {
        return itemType.isItemValid(itemStack);
    }

    public ItemStack getNewStack() {
        return itemType.createItemStack(amountPerPackage);
    }

    public String itemName() {
        return itemType.itemName();
    }

    public Material getItemType() {
        return itemType.getItemType();
    }

    public int getAmountPerPackage() {
        return amountPerPackage;
    }

    public void setAmountPerPackage(int amountPerPackage) {
        this.amountPerPackage = amountPerPackage;
    }

    public ShopAction getAction() {
        return action;
    }

    public void setAction(ShopAction action) {
        this.action = action;
    }

    public void display() {
        Block block = location.getLocation().getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (itemType == null) {
                sign.setLine(0, ChatColor.AQUA + "Boutique");
                sign.setLine(1, ChatColor.RED + "Non configuré");
                sign.setLine(3, ChatColor.RED + "<CLIC DROIT>");
            } else {
                sign.setLine(0, owner().shortDisplayable());
                if (action == ShopAction.BUY) {
                    sign.setLine(1, ChatColor.GREEN + "achète " + amountPerPackage);
                } else {
                    sign.setLine(1, ChatColor.BLUE + "vend " + amountPerPackage);
                }
                sign.setLine(2, ChatColor.BOLD + itemName());
                sign.setLine(3, ChatColor.BLUE + "Prix : " + price);
            }

            Bukkit.getScheduler().runTask(RPMachine.getInstance(), (Runnable) sign::update);
        } else {
            Bukkit.getLogger().info("Error : sign is not a sign, at " + location.toString());
        }
    }

    @Override
    public void breakSign() {
        for (; available > 0; available--) {
            Bukkit.getWorld("world").dropItemNaturally(location.getLocation(), getNewStack());
        }

        super.breakSign();
    }

    void clickPrivileged(Player player, RoleToken tt, PlayerInteractEvent event) {
        if (itemType == null) {
            if (event.getItem() == null)
                return;

            Material type = event.getItem().getType();

            if (action == ShopAction.SELL) {
                if (!tt.checkDelegatedPermission(ShopPermissions.CREATE_SELL_SHOPS))
                    return;

                if (!RPMachine.getInstance().getJobsManager().isFreeToSell(type)) {
                    if (!(tt.getLegalEntity() instanceof AdminLegalEntity)) {
                        if (tt.getLegalEntity() instanceof PlayerData) {
                            if (!RPMachine.getInstance().getJobsManager().canSell(player, type)) {
                                RPMachine.getInstance().getJobsManager().printAvailableJobsToSell(type, player);
                                return;
                            }

                            // Else we continue
                        } else {
                            player.sendMessage(ChatColor.RED + "Cet objet est restreint et ne peut être vendu que par des joueurs.");
                            return;
                        }
                    }
                }
            } else if (!tt.checkDelegatedPermission(ShopPermissions.CREATE_BUY_SHOPS))
                return;

            itemType = ItemStackStorage.init(event.getItem());
            if (itemType == null) {
                player.sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.RED + "Une erreur s'est produite.");
                return;
            } else if (itemType.maxAmount() < amountPerPackage) {
                player.sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.RED + "Cet item ne peut pas être vendu en lots de plus de " + itemType.maxAmount() + ".");
                itemType = null;
                return;
            }

            player.sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.GREEN + "Votre shop est maintenant totalement opérationnel.");
            if (action == ShopAction.SELL) {
                player.sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.GREEN + "Cliquez droit avec des items pour les ajouter dans l'inventaire de votre shop.");
            } else {
                player.sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.GREEN + "Cliquez droit pour récupérer le contenu de l'inventaire de votre shop.");
            }
            player.sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.GREEN + "Casser le panneau droppera l'inventaire du shop au sol.");
            display();
        } else {
            if (owner() instanceof AdminLegalEntity) {
                clickUser(player, event);
                return;
            }

            if (action == ShopAction.SELL && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (isItemValid(event.getItem())) {
                    if (!tt.checkDelegatedPermission(ShopPermissions.REFILL_SHOP))
                        return;

                    int amt = event.getItem().getAmount();
                    available += amt;
                    event.getPlayer().setItemInHand(new ItemStack(Material.AIR, 1));
                    event.getPlayer().sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.GREEN + "Vous venez d'ajouter " + ChatColor.AQUA + amt + ChatColor.GREEN + " ressources à votre shop. Il y en a maintenant " + ChatColor.AQUA + available + ChatColor.GREEN + " au total.");
                } else {
                    event.getPlayer().sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.YELLOW + "Il y a actuellement " + ChatColor.GOLD + this.available + ChatColor.YELLOW + " items dans la réserve de ce shop.");
                }
            } else {
                if (!tt.checkDelegatedPermission(ShopPermissions.GET_SHOP_STOCK))
                    return;

                if (player.getInventory().firstEmpty() == -1) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas assez de place dans votre inventaire.");
                } else {
                    if (available >= amountPerPackage) {
                        player.getInventory().addItem(getNewStack());
                        available -= amountPerPackage;
                        event.getPlayer().sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.GREEN + "Vous venez de récupérer " + ChatColor.AQUA + amountPerPackage + ChatColor.GREEN + " ressources à votre shop. Il en reste " + ChatColor.AQUA + available + ChatColor.GREEN + ".");
                    } else {
                        player.sendMessage(ShopsManager.SHOPS_PREFIX + ChatColor.RED + "Il n'y a aucun item à récupérer.");
                    }
                }
            }
        }
    }

    void clickUser(Player player, PlayerInteractEvent event) {
        RoleToken token = RPMachine.getPlayerRoleToken(player);

        if (event.getPlayer().isSneaking() && itemType != null) {
            player.sendMessage(ChatColor.GRAY + "Ce shop vend " + ChatColor.YELLOW + itemType.longItemName());
            return;
        }

        if (itemType == null) {
            player.sendMessage(ChatColor.RED + "Le créateur de ce shop n'a pas terminé sa configuration.");
        } else if (action == ShopAction.BUY) {
            if (!token.checkDelegatedPermission(ShopPermissions.SELL_ITEMS))
                return;

            ItemStack click = event.getItem();
            if (isItemValid(click) && click.getAmount() >= amountPerPackage) {
                if (owner().transfer(price, token.getLegalEntity())) {
                    Messages.creditEntity(player, token.getLegalEntity(), price, "vente de " + amountPerPackage + " " + itemType.longItemName());
                    available += amountPerPackage;
                    click.setAmount(click.getAmount() - amountPerPackage);
                } else {
                    player.sendMessage(ChatColor.RED + "L'acheteur n'a plus assez d'argent.");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Vous devez cliquer sur la panneau en tenant " + ChatColor.AQUA + itemType.longItemName() + ChatColor.RED + " en main.");
            }
        } else if (action == ShopAction.SELL) {
            if (!token.checkDelegatedPermission(ShopPermissions.BUY_ITEMS))
                return;

            if (available < amountPerPackage && !(owner() instanceof AdminLegalEntity) /* Admin shop : unlimited resources */) {
                player.sendMessage(ChatColor.RED + "Il n'y a pas assez d'items à vendre.");
                return;
            } else if (player.getInventory().firstEmpty() == -1) {
                player.sendMessage(ChatColor.RED + "Vous n'avez pas assez de place dans votre inventaire.");
                return;
            }

            if (token.getLegalEntity().transfer(price, owner())) {
                Messages.debitEntity(player, token.getLegalEntity(), price, "achat de " + amountPerPackage + " " + itemType.longItemName());
                available -= amountPerPackage;
                player.getInventory().addItem(getNewStack());
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
        p.sendMessage(ChatColor.YELLOW + "Action : " + getAction());
        p.sendMessage(ChatColor.YELLOW + "Item : " + itemName());
        p.sendMessage(ChatColor.YELLOW + "Amount per package : " + getAmountPerPackage());
        p.sendMessage(ChatColor.YELLOW + "Available items : " + getAvailable());
    }

    @Override
    public String describe() {
        String typeLine = getAction() == ItemShopSign.ShopAction.BUY ? net.md_5.bungee.api.ChatColor.RED + "Achat" : net.md_5.bungee.api.ChatColor.GREEN + "Vente";
        String size = (getAvailable() > getAmountPerPackage() ? net.md_5.bungee.api.ChatColor.GREEN : net.md_5.bungee.api.ChatColor.RED) + "" + getAvailable() + " en stock";

        return super.describe() + typeLine + ChatColor.YELLOW + " de lots de " + amountPerPackage + " " + itemType +
                " pour " + ChatColor.AQUA + price + RPMachine.getCurrencyName() + ChatColor.YELLOW +
                " (" + size + ChatColor.YELLOW + ")";
    }

    public enum ShopAction {
        SELL,
        BUY
    }

    public static class Builder extends ShopBuilder<ItemShopSign> {
        @Override
        public void describeFormat(Player player) {
            player.sendMessage(ChatColor.YELLOW + " - Shop");
            player.sendMessage(ChatColor.AQUA + " - Prix par lot");
            player.sendMessage(ChatColor.AQUA + " - Taille des lots");
            player.sendMessage(ChatColor.AQUA + " - Achat ou Vente");
        }

        @Override
        public boolean hasPermission(RoleToken player) {
            return player.hasDelegatedPermission(ShopPermissions.CREATE_SELL_SHOPS) ||
                    player.hasDelegatedPermission(ShopPermissions.CREATE_BUY_SHOPS);
        }

        public void postCreateInstructions(Player player) {
            player.sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] " + ChatColor.GREEN + "Votre boutique est presque prête ! Cliquez droit avec un item pour l'initialiser.");
        }

        @Override
        public Optional<ItemShopSign> parseSign(Block block, RoleToken tt, String[] lines) throws SignPermissionError, SignParseError {
            return Optional.of(new ItemShopSign(block.getLocation()))
                    .flatMap(sign -> extractPrice(lines[1]).map(price -> {
                        if (price > 100_000_000_000D)
                            throw new SignParseError("Le prix maximal est dépassé (100 milliards)");
                        sign.price = price;
                        return sign;
                    }))
                    .flatMap(sign -> extractInt(lines[2]).map(bundleSize -> {
                        if (bundleSize > 64)
                            throw new SignParseError("Impossible de vendre ou acheter des lots de plus de 64 items.");
                        sign.amountPerPackage = bundleSize;
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
