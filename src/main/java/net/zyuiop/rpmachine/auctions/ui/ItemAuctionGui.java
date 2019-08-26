package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.*;
import net.zyuiop.rpmachine.entities.AdminLegalEntity;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.permissions.ShopPermissions;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import net.zyuiop.rpmachine.utils.MenuItem;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Louis Vialar
 */
public class ItemAuctionGui extends Window {
    private final Material mat;

    public ItemAuctionGui(Material material, Player player) {
        super(9, "Hôtel Des Ventes " + material, player);
        this.mat = material;
    }

    @Override
    public void fill() {
        double minPrice = AuctionManager.INSTANCE.minPrice(mat);
        double avgPrice = AuctionManager.INSTANCE.averagePrice(mat);

        int avail = AuctionManager.INSTANCE.countAvailable(mat);
        String desc =
                ChatColor.YELLOW + "Prix moyen: " + (avgPrice != avgPrice ? ChatColor.RED + "Inconnu" : ChatColor.AQUA + String.format("%.2f", avgPrice)) + RPMachine.getCurrencyName() + "\n" +
                        ChatColor.YELLOW + "Prix minimum: " + (minPrice != minPrice ? ChatColor.RED + "Inconnu" : ChatColor.AQUA + String.format("%.2f", minPrice)) + RPMachine.getCurrencyName() + "\n" +
                        ChatColor.YELLOW + "Disponible: " + avail;

        setItem(4, new MenuItem(mat).setName(mat.name()).setDescriptionBlock(desc +
                "\n" + ChatColor.GREEN +
                "\n" + ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + "Clic : voir le détail des offres de vente"
        ), () -> {

            close();
            new AuctionsListGui(this, mat, AuctionManager.INSTANCE.getAuctions(mat)).open();
        });

        LegalEntity token = RPMachine.getPlayerActAs(player);

        if (token.hasDelegatedPermission(player, ShopPermissions.BUY_ITEMS) && avail > 0) {
            setItem(0, new MenuItem(Material.GOLD_INGOT).setName("Acheter automatiquement").setDescriptionBlock(desc), () -> {
                close();
                new BuyGui(player, mat, true).open();
            });

            setItem(1, new MenuItem(Material.GOLD_BLOCK).setName("Placer offre d'achat").setDescription(ChatColor.YELLOW + "Indiquez votre offre d'achat et payez immédiatement", ChatColor.YELLOW + "Collectez les items au fur et à mesure"), () -> {
                close();
                new BuyGui(player, mat, false).open();
            });
        }


        if (token.hasDelegatedPermission(player, ShopPermissions.SELL_ITEMS)) {
            setItem(7, new MenuItem(Material.CHEST).setName("Placer offre de vente").setDescriptionBlock(desc), () -> {
                close();
                new SellGui(player, mat, avgPrice, minPrice).open();
            });

            setItem(8, new MenuItem(Material.ENDER_CHEST).setName("Vendre automatiquement").setDescription(ChatColor.YELLOW + "Vendez directement aux offres d'achat placées par d'autres joueurs", ChatColor.RED + "Vous ne choissez pas le prix, mais pouvez le refuser si trop bas.", ChatColor.GREEN + "Le produit de la vente est crédité immédiatement"), () -> {
                close();
                putOnSaleAuto();
            });
        }

        if (token.hasDelegatedPermission(player, ShopPermissions.GET_SHOP_STOCK)) {
            List<SellOrder> my = AuctionManager.INSTANCE.getMyAuctions(mat, token);

            if (!my.isEmpty()) {
                setItem(6, new MenuItem(Material.RED_SHULKER_BOX).setName("Mes ventes").setDescription(ChatColor.YELLOW + "Pour lister et retirer certaines de vos ventes en cours"), () -> {
                    close();
                    new MyAuctionsGui(this, mat, my).open();
                });
            }

            List<BuyOrder> myBuys = AuctionManager.INSTANCE.getMyBuys(mat, token);

            if (!myBuys.isEmpty()) {
                setItem(2, new MenuItem(Material.GREEN_SHULKER_BOX).setName("Mes achats").setDescription(ChatColor.YELLOW + "Pour lister et retirer certaines de vos offres d'achat en cours"), () -> {
                    close();
                    new MyBuysGui(this, mat, myBuys).open();
                });
            }
        }
    }

    @Override
    public void open() {
        if (RPMachine.getPlayerActAs(player) == AdminLegalEntity.INSTANCE) {
            player.sendMessage(ChatColor.RED + "La Confédération ne peut pas utiliser l'hôtel des ventes.");
            return;
        }

        super.open();
    }

    private void putOnSaleAuto() {
        Inventory inventory = Bukkit.createInventory(player, 6 * 9, "Items à vendre au plus offrant");

        AuctionInventoryListener.INSTANCE.addPlayer(player, () -> {
            Bukkit.getLogger().info("Closing AUTO SELL inventory for player " + player.getName() + " -- item " + mat);

            int count = 0;
            for (ItemStack stack : inventory.getContents()) {
                if (stack == null) continue;

                if (stack.getType() != mat) {
                    Bukkit.getLogger().info(" --> Putting back stack " + stack.getType() + " x " + stack.getAmount() + " " + stack);

                    // Illegal items go back to their country
                    player.getInventory().addItem(stack);
                } else {
                    // Right material, add to the count
                    count += stack.getAmount();
                    Bukkit.getLogger().info(" --> Adding " + stack.getAmount() + " (count " + count + ")");
                }
            }

            player.closeInventory();

            int finalCount = count;
            SellTransaction sell = AuctionManager.INSTANCE.startSell(RPMachine.getPlayerActAs(player), mat, finalCount);

            if (sell == null) {
                InventoryUtils.giveItems(mat, finalCount, player.getInventory());
                player.saveData();
                player.sendMessage(ChatColor.RED + "Aucune offre d'achat disponible...");
                return;
            }

            int diff = finalCount - sell.getTotalAmount();
            if (diff > 0)
                player.sendMessage(ChatColor.YELLOW + "Trop d'items mis en vente, pas assez d'offres d'achat. Nous vous rendons " + diff + " items.");
            while (diff > 0) {
                int stackSize = Math.min(diff, mat.getMaxStackSize());
                ItemStack stack = new ItemStack(mat, stackSize);
                player.getInventory().addItem(stack);
                diff -= stackSize;
            }

            Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(), () -> {
                        if (!player.isOnline()) {
                            Bukkit.getLogger().info("Player went away, giving back items");
                            InventoryUtils.giveItems(mat, sell.getTotalAmount(), player.getInventory());

                            player.saveData();
                            return;
                        }
                        Bukkit.getLogger().info("Opening confirm sale window for " + player.getName() + " / " + mat + " / " + finalCount + " total items");

                        new TransactionConfirmGui(player, mat, sell).open();
                    }
                    , 1);
        });

        player.openInventory(inventory);
    }

}
