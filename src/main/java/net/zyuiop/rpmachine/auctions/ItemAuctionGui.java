package net.zyuiop.rpmachine.auctions;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.entities.AdminLegalEntity;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.gui.ConfirmGui;
import net.zyuiop.rpmachine.gui.PickNumberGui;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
                        ChatColor.YELLOW + "Disponible: " + avail
        ;

        setItem(4, new MenuItem(mat).setName(mat.name()).setDescriptionBlock(desc), () -> {
        });

        LegalEntity token = RPMachine.getPlayerActAs(player);

        if (token.hasDelegatedPermission(player, ShopPermissions.BUY_ITEMS) && avail > 0)
            setItem(1, new MenuItem(Material.GOLD_INGOT).setName("Acheter").setDescriptionBlock(desc), () -> {
                close();
                new BuyGui().open();
            });


        if (token.hasDelegatedPermission(player, ShopPermissions.SELL_ITEMS))
            setItem(7, new MenuItem(Material.CHEST).setName("Vendre").setDescriptionBlock(desc), () -> {
                close();
                new SellGui(avgPrice).open();
            });

        if (token.hasDelegatedPermission(player, ShopPermissions.GET_SHOP_STOCK)) {
            List<Auction> my = AuctionManager.INSTANCE.getMyAuctions(mat, token);

            if (!my.isEmpty()) {
                setItem(6, new MenuItem(Material.RED_SHULKER_BOX).setName("Mes enchères").setDescription(ChatColor.YELLOW + "Pour lister et retirer certaines de vos ventes en cours"), () -> {
                    close();
                    new MyAuctionsGui(my).open();
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

    class MyAuctionsGui extends Window {
        private List<Auction> myAuctions;

        MyAuctionsGui(List<Auction> myAuctions) {
            super((int) (Math.ceil((myAuctions.size() + 3) / 9D) * 9D), "Mes enchères en cours", ItemAuctionGui.this.player);
            this.myAuctions = myAuctions;
        }

        @Override
        public void fill() {
            setItem(size - 1, new MenuItem(Material.ARROW).setName(ChatColor.YELLOW + "Retour"), () -> {
                close();
                ItemAuctionGui.this.open();
            });

            setItem(size - 2, new MenuItem(Material.WATER_BUCKET).setName(ChatColor.AQUA + "Raffraichir"), this::refresh);

            load();
        }

        private void refresh() {
            myAuctions = AuctionManager.INSTANCE.getMyAuctions(mat, RPMachine.getPlayerActAs(player));

            clear();

            fill();
        }

        private void load() {
            for (int i = 0; i < (myAuctions.size()) && i < size - 3; ++i) {
                Auction a = myAuctions.get(i);

                setItem(i,
                        new MenuItem(mat, Math.min(mat.getMaxStackSize(), a.getAvailable()))
                                .setDescription(
                                        ChatColor.YELLOW + "Unités restantes : " + ChatColor.GOLD + a.getAvailable(),
                                        ChatColor.YELLOW + "Prix unitaire : " + ChatColor.GOLD + String.format("%.2f", a.getItemPrice()) + RPMachine.getCurrencyName(),
                                        ChatColor.GOLD + "",

                                        ChatColor.YELLOW + "Prix min actuel : " + ChatColor.GOLD + String.format("%.2f", AuctionManager.INSTANCE.minPrice(mat)) + RPMachine.getCurrencyName(),
                                        ChatColor.GOLD + "",
                                        ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + " Clic : récupérer le lot"
                                )
                        , () -> {
                            int availableSize = InventoryUtils.availablePlaceFor(player.getInventory(), mat);

                            if (availableSize < a.getAvailable()) {
                                player.sendMessage(ChatColor.RED + "Pas assez de place ! Maximum dispo : " + availableSize + " items.");
                                refresh();
                                return;
                            }

                            if (AuctionManager.INSTANCE.removeAuction(a)) {
                                InventoryUtils.giveItems(mat, a.getAvailable(), player.getInventory());
                                player.sendMessage(ChatColor.GREEN + "Offre récupérée !");
                                refresh();
                            } else {
                                player.sendMessage(ChatColor.RED + "Cette offre n'est plus disponible.");
                                refresh();
                            }

                        });
            }
        }
    }

    class BuyGui extends PickNumberGui {
        private double avgPrice = AuctionManager.INSTANCE.averagePrice(mat);

        protected BuyGui() {
            super("Combien de " + mat + " acheter ?", ItemAuctionGui.this.player, 10, 5, 1, 1);
            this.min = 1;
            this.max = InventoryUtils.availablePlaceFor(player.getInventory(), mat);
        }


        @Override
        public MenuItem updateItem(double value) {
            return new MenuItem(mat, value > mat.getMaxStackSize() ? mat.getMaxStackSize() : (int) value).setName("" + (int) value)
                    .setDescription(ChatColor.YELLOW + "Prix estimé " + ChatColor.AQUA + String.format("%.2f", avgPrice * value));
        }

        @Override
        protected void finish(double value) {
            AuctionManager.Transaction tx = AuctionManager.INSTANCE.startTransaction(RPMachine.getPlayerActAs(player), mat, (int) value);

            if (tx == null) {
                player.sendMessage(ChatColor.RED + "Aucun item en vente...");
                return;
            } else if (tx.getRequestedItems() < value) {
                player.sendMessage(ChatColor.YELLOW + "Attention, quantité réduite à " + tx.getRequestedItems());
            }

            new TransactionConfirmGui(tx).open();
        }
    }

    class SellGui extends PickNumberGui {
        private double avgPrice;

        protected SellGui(double avgPrice) {
            super("A quel prix unitaire vendre ?", ItemAuctionGui.this.player, 5, .5, .05, avgPrice != avgPrice ? 0D : avgPrice);
            this.avgPrice = avgPrice;
            this.min = 0D;
        }

        @Override
        public MenuItem updateItem(double value) {
            return new MenuItem(mat).setName(String.format("%.2f", value) + RPMachine.getCurrencyName())
                    .setDescription(ChatColor.YELLOW + "Prix moyen " + ChatColor.AQUA + String.format("%.2f", avgPrice) + RPMachine.getCurrencyName());
        }

        @Override
        protected void finish(double value) {
            player.sendMessage(ChatColor.YELLOW + "Vente au prix unitaire de " + ChatColor.AQUA + String.format("%.2f", value) + RPMachine.getCurrencyName());
            player.sendMessage(ChatColor.YELLOW + "Mettez les items à vendre dans l'inventaire qui va s'ouvrir...");
            player.sendMessage(ChatColor.YELLOW + "Fermez l'inventaire pour valider :=)");

            Inventory inventory = Bukkit.createInventory(player, 6 * 9, "Vendre à " + String.format("%.2f", value) + RPMachine.getCurrencyName() + " /unit");

            player.openInventory(inventory);
            AuctionInventoryListener.INSTANCE.addPlayer(player, () -> {
                int count = 0;
                for (ItemStack stack : inventory.getContents()) {
                    if (stack == null) continue;

                    if (stack.getType() != mat) {
                        // Illegal items go back to their country

                        player.getInventory().addItem(stack);
                    } else {
                        // Right material, add to the count
                        count += stack.getAmount();
                    }
                }

                player.closeInventory();

                int finalCount = count;
                Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(), () -> {
                            if (!player.isOnline()) {
                                Bukkit.getLogger().info("Player went away, giving back items");
                                InventoryUtils.giveItems(mat, finalCount, player.getInventory());

                                player.saveData();
                                return;
                            }

                            new PutOnSaleConfirmGui(value, finalCount).open();
                        }
                        , 1);
            });

        }
    }

    class PutOnSaleConfirmGui extends ConfirmGui {
        private final double price;
        private final int quantity;

        protected PutOnSaleConfirmGui(double price, int quantity) {
            super("Confirmer vente de " + quantity + " " + mat + " pour " + String.format("%.2f", price * quantity), ItemAuctionGui.this.player);
            this.price = price;
            this.quantity = quantity;
        }

        @Override
        protected MenuItem createInfoItem() {
            return new MenuItem(mat, Math.min(quantity, mat.getMaxStackSize())).setName(ChatColor.YELLOW + "" + quantity)
                    .setDescription(
                            ChatColor.YELLOW + "Prix unit: " + String.format("%.2f", price),
                            ChatColor.YELLOW + "Prix total: " + String.format("%.2f", price * quantity)
                    );
        }

        @Override
        protected void finish(boolean accepted) {
            if (accepted) {
                AuctionManager.INSTANCE.addAuction(new Auction(mat, price, quantity, RPMachine.getPlayerRoleToken(player).getTag()));
                AuctionManager.INSTANCE.save();

                player.sendMessage(ChatColor.GREEN + "Mise en vente réussie !");
            } else {
                player.sendMessage(ChatColor.RED + "Mise en vente annulée.");

                InventoryUtils.giveItems(mat, quantity, player.getInventory());
            }
        }
    }

    class TransactionConfirmGui extends ConfirmGui {
        private final AuctionManager.Transaction transaction;
        private final BukkitTask task;

        TransactionConfirmGui(AuctionManager.Transaction transaction) {
            super("Prix total: " + String.format("%.2f", transaction.getPrice()) + ". OK ?", ItemAuctionGui.this.player);
            this.transaction = transaction;

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    updateInfoItem();

                    if (transaction.checkAutoCancel()) {
                        this.cancel();
                        TransactionConfirmGui.this.close(true);
                    }
                }
            }.runTaskTimer(RPMachine.getInstance(), 10L, 20L);
        }

        private void updateInfoItem() {
            setItem(1, 4, createInfoItem(), () -> {
            });
        }

        @Override
        protected MenuItem createInfoItem() {
            return new MenuItem(mat, transaction.remainingSeconds()).setName(ChatColor.YELLOW + "Valider prix " + transaction.getPrice() + RPMachine.getCurrencyName())
                    .setDescription(ChatColor.YELLOW + "" + transaction.getRequestedItems() + " * " + ChatColor.GOLD + mat,
                            ChatColor.YELLOW + "",
                            ChatColor.YELLOW + "Expiration dans " + ChatColor.RED + transaction.remainingSeconds() + " sec");
        }

        @Override
        protected void finish(boolean accepted) {
            task.cancel();
            if (accepted) {
                transaction.complete(player);
            } else {
                player.sendMessage(ChatColor.RED + "Transaction annulée.");
                transaction.cancel();
                AuctionManager.INSTANCE.save();
            }
        }
    }
}
