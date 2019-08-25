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
import net.zyuiop.rpmachine.utils.Messages;
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
import java.util.TreeSet;

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
            new AuctionsListGui(AuctionManager.INSTANCE.getAuctions(mat)).open();
        });

        LegalEntity token = RPMachine.getPlayerActAs(player);

        if (token.hasDelegatedPermission(player, ShopPermissions.BUY_ITEMS) && avail > 0) {
            setItem(0, new MenuItem(Material.GOLD_INGOT).setName("Acheter automatiquement").setDescriptionBlock(desc), () -> {
                close();
                new BuyGui(true).open();
            });

            setItem(1, new MenuItem(Material.GOLD_BLOCK).setName("Placer offre d'achat").setDescription(ChatColor.YELLOW + "Indiquez votre offre d'achat et payez immédiatement", ChatColor.YELLOW + "Collectez les items au fur et à mesure"), () -> {
                close();
                new BuyGui(false).open();
            });
        }


        if (token.hasDelegatedPermission(player, ShopPermissions.SELL_ITEMS)) {
            setItem(7, new MenuItem(Material.CHEST).setName("Placer offre de vente").setDescriptionBlock(desc), () -> {
                close();
                new SellGui(avgPrice, minPrice).open();
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
                    new MyAuctionsGui(my).open();
                });
            }

            List<BuyOrder> myBuys = AuctionManager.INSTANCE.getMyBuys(mat, token);

            if (!myBuys.isEmpty()) {
                setItem(2, new MenuItem(Material.GREEN_SHULKER_BOX).setName("Mes achats").setDescription(ChatColor.YELLOW + "Pour lister et retirer certaines de vos offres d'achat en cours"), () -> {
                    close();
                    new MyBuysGui(myBuys).open();
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
        private List<SellOrder> myAuctions;

        MyAuctionsGui(List<SellOrder> myAuctions) {
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
                SellOrder a = myAuctions.get(i);

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

    class MyBuysGui extends Window {
        private List<BuyOrder> myAuctions;

        MyBuysGui(List<BuyOrder> myAuctions) {
            super((int) (Math.ceil((myAuctions.size() + 3) / 9D) * 9D), "Mes achats en cours", ItemAuctionGui.this.player);
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
            myAuctions = AuctionManager.INSTANCE.getMyBuys(mat, RPMachine.getPlayerActAs(player));

            clear();

            fill();
        }

        private void load() {
            for (int i = 0; i < (myAuctions.size()) && i < size - 3; ++i) {
                BuyOrder a = myAuctions.get(i);

                double refund = a.getItemPrice() * a.getRemainingItems();

                setItem(i,
                        new MenuItem(mat, Math.max(1, Math.min(mat.getMaxStackSize(), a.getAvailable())))
                                .setDescription(
                                        ChatColor.YELLOW + "Quantité disponible : " + ChatColor.GOLD + a.getAvailable(),
                                        ChatColor.YELLOW + "Quantité totale à acheter : " + ChatColor.GOLD + a.getMaxBuy(),
                                        ChatColor.YELLOW + "Quantité restant à acheter : " + ChatColor.GOLD + a.getRemainingItems(),
                                        ChatColor.YELLOW + " ",
                                        ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + " Clic : récupérer le lot",
                                        ChatColor.GRAY + "Votre offre sera retirée, vous obtiendrez vos items et " + String.format("%.4f", refund) + RPMachine.getCurrencyName()
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
                                double actualRefund = a.getItemPrice() * a.getRemainingItems();
                                RPMachine.getPlayerActAs(player).creditMoney(actualRefund);
                                Messages.credit(RPMachine.getPlayerActAs(player), actualRefund, "retrait d'une offre d'achat");
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

    class AuctionsListGui extends Window {
        private TreeSet<SellOrder> auctions;

        AuctionsListGui(TreeSet<SellOrder> auctions) {
            super((int) (Math.min(6, Math.ceil((auctions.size() + 3) / 9D)) * 9D), "Enchères en cours", ItemAuctionGui.this.player);
            this.auctions = auctions;
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
            auctions = AuctionManager.INSTANCE.getAuctions(mat);
            clear();
            fill();
        }

        private void load() {
            int i = 0;

            for (SellOrder a : auctions) {
                if (i > size - 3)
                    return;

                setItem(i,
                        new MenuItem(mat, Math.min(mat.getMaxStackSize(), a.getAvailable()))
                                .setDescription(
                                        ChatColor.YELLOW + "Quantité vendue : " + ChatColor.GOLD + a.getAvailable(),
                                        ChatColor.YELLOW + "Prix unitaire : " + ChatColor.GOLD + String.format("%.2f", a.getItemPrice()) + RPMachine.getCurrencyName(),
                                        ChatColor.YELLOW + "Vendeur : " + a.owner().shortDisplayable(),
                                        ChatColor.GOLD + "",

                                        ChatColor.YELLOW + "Prix du lot : " + ChatColor.GOLD + String.format("%.2f", (a.getAvailable() * a.getItemPrice())) + RPMachine.getCurrencyName(),
                                        ChatColor.GOLD + "",
                                        ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + " Clic : acheter le lot"
                                )
                        , () -> {
                            if (AuctionManager.INSTANCE.removeAuction(a)) {
                                close();

                                new TransactionConfirmGui(AuctionManager.INSTANCE.startBuyRemovedOrder(RPMachine.getPlayerActAs(player), a)).open();
                            } else {
                                player.sendMessage(ChatColor.RED + "Cette offre n'est plus disponible.");
                                refresh();
                            }
                        });

                ++i;
            }
        }
    }

    class BuyGui extends PickNumberGui {
        private double avgPrice = AuctionManager.INSTANCE.averagePrice(mat);
        private boolean autoPrice;

        protected BuyGui(boolean autoPrice) {
            super("Combien de " + mat + " acheter ?", ItemAuctionGui.this.player, 10, 5, 1, 1);
            this.autoPrice = autoPrice;
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
            // Here use autoprice
            if (autoPrice) {

                BuyTransaction tx = AuctionManager.INSTANCE.startBuy(RPMachine.getPlayerActAs(player), mat, (int) value);

                if (tx == null) {
                    player.sendMessage(ChatColor.RED + "Aucun item en vente...");
                    return;
                } else if (tx.getTotalAmount() < value) {
                    player.sendMessage(ChatColor.YELLOW + "Attention, quantité réduite à " + tx.getTotalAmount());
                }

                new TransactionConfirmGui(tx).open();
            } else {
                new BuySetPriceGui((int) value, avgPrice, AuctionManager.INSTANCE.minPrice(mat)).open();

            }
        }
    }

    class BuySetPriceGui extends PickNumberGui {
        private int quantity;
        private double avgPrice;
        private double minPrice;

        protected BuySetPriceGui(int quantity, double avgPrice, double minPrice) {
            super("A quel prix unitaire acheter ?", ItemAuctionGui.this.player, 5, .25, .01, avgPrice != avgPrice ? 0D : avgPrice);
            this.quantity = quantity;
            this.avgPrice = avgPrice;
            this.minPrice = minPrice;
            this.min = 0D;
        }

        @Override
        public MenuItem updateItem(double value) {
            return new MenuItem(mat).setName(String.format("%.2f", value) + RPMachine.getCurrencyName())
                    .setDescription(ChatColor.YELLOW + "Prix moyen " + ChatColor.AQUA + String.format("%.2f", avgPrice) + RPMachine.getCurrencyName(),
                            ChatColor.YELLOW + "Prix minimal " + ChatColor.AQUA + String.format("%.2f", minPrice) + RPMachine.getCurrencyName()
                    );
        }

        @Override
        protected void finish(double value) {
            close();
            double price = value * quantity;

            if (RPMachine.getPlayerActAs(player).getBalance() < price) {
                Messages.notEnoughMoneyEntity(player, RPMachine.getPlayerActAs(player), price);
                return;
            }
            new BuyOrderConfirmGui(value, quantity).open();
        }
    }

    class BuyOrderConfirmGui extends ConfirmGui {
        private final double price;
        private final int quantity;

        protected BuyOrderConfirmGui(double price, int quantity) {
            super("Confirmer achat de " + quantity + " " + mat + " pour " + String.format("%.2f", price * quantity), ItemAuctionGui.this.player);
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
                if (RPMachine.getPlayerActAs(player).withdrawMoney(price * quantity)) {
                    Messages.debit(RPMachine.getPlayerActAs(player), price * quantity, "ordre d'achat pour " + quantity + " * " + mat);
                    AuctionManager.INSTANCE.addAuction(new BuyOrder(mat, price, RPMachine.getPlayerRoleToken(player).getTag(), quantity));
                    AuctionManager.INSTANCE.save();

                    player.sendMessage(ChatColor.GREEN + "Ordre d'achat envoyé ! Revenez régulièrement vérifier l'état de votre ordre !");
                } else {
                    Messages.notEnoughMoneyEntity(player, RPMachine.getPlayerActAs(player), price);
                }
            } else {
                player.sendMessage(ChatColor.RED + "Achat annulé.");
            }
        }
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

                        new TransactionConfirmGui(sell).open();
                    }
                    , 1);
        });

        player.openInventory(inventory);
    }

    class SellGui extends PickNumberGui {
        private double avgPrice;
        private double minPrice;

        protected SellGui(double avgPrice, double minPrice) {
            super("A quel prix unitaire vendre ?", ItemAuctionGui.this.player, 5, .25, .01, avgPrice != avgPrice ? 0D : avgPrice);
            this.avgPrice = avgPrice;
            this.minPrice = minPrice;
            this.min = 0D;
        }

        @Override
        public MenuItem updateItem(double value) {
            return new MenuItem(mat).setName(String.format("%.2f", value) + RPMachine.getCurrencyName())
                    .setDescription(
                            ChatColor.YELLOW + "Prix moyen " + ChatColor.AQUA + String.format("%.2f", avgPrice) + RPMachine.getCurrencyName(),
                            ChatColor.YELLOW + "Prix minimal " + ChatColor.AQUA + String.format("%.2f", minPrice) + RPMachine.getCurrencyName()
                    );
        }

        @Override
        protected void finish(double value) {
            player.sendMessage(ChatColor.YELLOW + "Vente au prix unitaire de " + ChatColor.AQUA + String.format("%.2f", value) + RPMachine.getCurrencyName());
            player.sendMessage(ChatColor.YELLOW + "Mettez les items à vendre dans l'inventaire qui va s'ouvrir...");
            player.sendMessage(ChatColor.YELLOW + "Fermez l'inventaire pour valider :=)");

            Bukkit.getLogger().info("Opening SELL inventory for player " + player.getName() + " -- item " + mat + " / price " + value);

            Inventory inventory = Bukkit.createInventory(player, 6 * 9, "Vendre à " + String.format("%.2f", value) + RPMachine.getCurrencyName() + " /unit");

            AuctionInventoryListener.INSTANCE.addPlayer(player, () -> {
                Bukkit.getLogger().info("Closing SELL inventory for player " + player.getName() + " -- item " + mat + " / price " + value);

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
                Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(), () -> {
                            if (!player.isOnline()) {
                                Bukkit.getLogger().info("Player went away, giving back items");
                                InventoryUtils.giveItems(mat, finalCount, player.getInventory());

                                player.saveData();
                                return;
                            }
                            Bukkit.getLogger().info("Opening confirm sale window for " + player.getName() + " / " + mat + " / " + finalCount + " total items");

                            new PutOnSaleConfirmGui(value, finalCount).open();
                        }
                        , 1);
            });

            player.openInventory(inventory);
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
            Bukkit.getLogger().info("Sale confirm window closed " + player.getName() + ", result was " + accepted);

            if (accepted) {
                AuctionManager.INSTANCE.addAuction(new SellOrder(mat, price, quantity, RPMachine.getPlayerRoleToken(player).getTag()));
                AuctionManager.INSTANCE.save();

                player.sendMessage(ChatColor.GREEN + "Mise en vente réussie !");
            } else {
                player.sendMessage(ChatColor.RED + "Mise en vente annulée.");

                InventoryUtils.giveItems(mat, quantity, player.getInventory());
            }
        }
    }

    class TransactionConfirmGui extends ConfirmGui {
        private final AbstractTransaction transaction;
        private final BukkitTask task;

        TransactionConfirmGui(AbstractTransaction transaction) {
            super("Prix total: " + String.format("%.2f", transaction.getPrice()) + ". OK ?", ItemAuctionGui.this.player);
            this.transaction = transaction;

            task = new BukkitRunnable() {
                @Override
                public void run() {
                    updateInfoItem();

                    if (transaction.checkAutoCancel()) {
                        AuctionManager.INSTANCE.save();
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

        private String getTransactionType() {
            return transaction instanceof SellTransaction ? ChatColor.GREEN + "VENTE" : ChatColor.YELLOW + "ACHAT";
        }

        @Override
        protected MenuItem createInfoItem() {
            return new MenuItem(mat, transaction.remainingSeconds()).setName(ChatColor.GRAY + "Valider " + getTransactionType() + ChatColor.GRAY + " pour " + ChatColor.AQUA + String.format("%.4f", transaction.getPrice()) + RPMachine.getCurrencyName())
                    .setDescription(ChatColor.YELLOW + "" + transaction.getTotalAmount() + " * " + ChatColor.GOLD + mat,
                            ChatColor.YELLOW + "",
                            ChatColor.YELLOW + "Expiration dans " + ChatColor.RED + transaction.remainingSeconds() + " sec");
        }

        @Override
        protected void finish(boolean accepted) {
            task.cancel();
            if (accepted) {
                transaction.complete(player);
                AuctionManager.INSTANCE.save();
            } else {
                player.sendMessage(ChatColor.RED + "Transaction annulée.");
                transaction.cancel();

                if (transaction instanceof SellTransaction) {
                    InventoryUtils.giveItems(mat, transaction.getTotalAmount(), player.getInventory());

                    if (!player.isOnline()) {
                        player.saveData();
                    }
                }

                AuctionManager.INSTANCE.save();
            }
        }
    }
}
