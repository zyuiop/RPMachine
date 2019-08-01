package net.zyuiop.rpmachine.shops.types;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.economy.Messages;
import net.zyuiop.rpmachine.economy.jobs.Job;
import net.zyuiop.rpmachine.entities.AdminLegalEntity;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.permissions.ShopPermissions;
import net.zyuiop.rpmachine.shops.ShopBuilder;
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
    public enum ShopAction {
        SELL,
        BUY
    }

    private Material itemType;
    private short damage;
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
        return itemStack != null && itemStack.getType() == itemType && itemStack.getDurability() == damage;
    }

    public ItemStack getNewStack() {
        return new ItemStack(itemType, amountPerPackage, damage);
    }

    public Material getItemType() {
        return itemType;
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
                sign.setLine(2, ChatColor.BOLD + itemType.toString());
                sign.setLine(3, ChatColor.BLUE + "Prix : " + price);
            }

            Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> sign.update());
        } else {
            Bukkit.getLogger().info("Error : sign is not a sign, at " + location.toString());
        }
    }

    @Override
    public void breakSign() {
        for (; available > 0; available--) {
            Bukkit.getWorld("world").dropItemNaturally(location.getLocation(), new ItemStack(itemType, 1, damage));
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

                Job job = RPMachine.getInstance().getJobsManager().getJob(player.getUniqueId());
                if (job == null) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas de métier pour le moment.");
                    return;
                }

                if (!job.getMaterials().contains(type)) {
                    player.sendMessage(ChatColor.RED + "Votre job ne vous permet pas de vendre cela.");
                    return;
                }
            } else if (!tt.checkDelegatedPermission(ShopPermissions.CREATE_BUY_SHOPS))
                return;

            itemType = type;
            damage = event.getItem().getDurability();
            player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Votre shop est maintenant totalement opérationnel.");
            if (action == ShopAction.SELL) {
                player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Cliquez droit avec des items pour les ajouter dans l'inventaire de votre shop.");
            } else {
                player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Cliquez droit pour récupérer le contenu de l'inventaire de votre shop.");
            }
            player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Casser le panneau droppera l'inventaire du shop au sol.");
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
                    event.getPlayer().sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Vous venez d'ajouter " + ChatColor.AQUA + amt + ChatColor.GREEN + " ressources à votre shop. Il y en a maintenant " + ChatColor.AQUA + available + ChatColor.GREEN + " au total.");
                } else {
                    event.getPlayer().sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.YELLOW + "Il y a actuellement " + ChatColor.GOLD + this.available + ChatColor.YELLOW + " items dans la réserve de ce shop.");
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
                        event.getPlayer().sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Vous venez de récupérer " + ChatColor.AQUA + amountPerPackage + ChatColor.GREEN + " ressources à votre shop. Il en reste " + ChatColor.AQUA + available + ChatColor.GREEN + ".");
                    } else {
                        player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.RED + "Il n'y a aucun item à récupérer.");
                    }
                }
            }
        }
    }

    void clickUser(Player player, PlayerInteractEvent event) {
        RoleToken token = RPMachine.getPlayerRoleToken(player);

        if (itemType == null) {
            player.sendMessage(ChatColor.RED + "Le créateur de ce shop n'a pas terminé sa configuration.");
        } else if (action == ShopAction.BUY) {
            if (!token.checkDelegatedPermission(ShopPermissions.SELL_ITEMS))
                return;

            ItemStack click = event.getItem();
            if (isItemValid(click) && click.getAmount() >= amountPerPackage) {
                EconomyManager manager = RPMachine.getInstance().getEconomyManager();
                manager.transferMoneyBalanceCheck(owner(), token.getLegalEntity(), price, result -> {
                    if (result) {
                        available += amountPerPackage;
                        player.sendMessage(Messages.RECEIVED_MONEY.getMessage().replace("{AMT}", "" + price).replace("{FROM}", owner().displayable()));
                        click.setAmount(click.getAmount() - amountPerPackage);
                        player.getInventory().setItemInHand(click);
                    } else {
                        player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.RED + "L'acheteur n'a plus assez d'argent pour cela.");
                    }
                });
            } else {
                player.sendMessage(ChatColor.RED + "Vous devez cliquer sur la panneau en tenant " + ChatColor.AQUA + itemType.toString() + ChatColor.RED + " en main.");
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

            EconomyManager manager = RPMachine.getInstance().getEconomyManager();
            manager.transferMoneyBalanceCheck(token.getLegalEntity(), owner(), price, result -> {
                if (result) {
                    player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Vous avez bien acheté " + amountPerPackage + itemType.toString() + " pour " + price + " " + EconomyManager.getMoneyName());
                    available -= amountPerPackage;
                    player.getInventory().addItem(getNewStack());
                } else {
                    player.sendMessage(Messages.NOT_ENOUGH_MONEY.getMessage());
                }
            });
        }
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
                    .flatMap(sign -> extractDouble(lines[1]).map(price -> {
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

    @Override
    public void debug(Player p) {
        p.sendMessage(ChatColor.YELLOW + "-----[ Débug Shop ] -----");
        p.sendMessage(ChatColor.YELLOW + "Price : " + getPrice());
        p.sendMessage(ChatColor.YELLOW + "Owner (Tag/displayable) : " + ownerTag() + " / " + owner().displayable());
        p.sendMessage(ChatColor.YELLOW + "Action : " + getAction());
        p.sendMessage(ChatColor.YELLOW + "Item : " + getItemType());
        p.sendMessage(ChatColor.YELLOW + "Amount per package : " + getAmountPerPackage());
        p.sendMessage(ChatColor.YELLOW + "Available items : " + getAvailable());
    }
}
