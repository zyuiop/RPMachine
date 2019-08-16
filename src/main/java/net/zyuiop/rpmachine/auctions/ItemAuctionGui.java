package net.zyuiop.rpmachine.auctions;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.gui.ConfirmGui;
import net.zyuiop.rpmachine.gui.PickNumberGui;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;

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

        String[] desc = {
                ChatColor.YELLOW + "Prix moyen: " + (avgPrice != avgPrice ? ChatColor.RED + "Inconnu" : ChatColor.AQUA + String.format("%.2f", avgPrice)) + RPMachine.getCurrencyName(),
                ChatColor.YELLOW + "Prix minimum: " + (minPrice != minPrice ? ChatColor.RED + "Inconnu" : ChatColor.AQUA + String.format("%.2f", minPrice)) + RPMachine.getCurrencyName()
        };

        setItem(4, new MenuItem(mat).setName(mat.name()).setDescription(desc), () -> {
        });
        setItem(1, new MenuItem(Material.GOLD_INGOT).setName("Acheter").setDescription(desc), () -> {
            close();
            new BuyGui().open();
        });
        setItem(7, new MenuItem(Material.CHEST).setName("Vendre").setDescription(desc), () -> {
            close();
            new SellGui(avgPrice).open();
        });
    }

    class BuyGui extends PickNumberGui {
        private double avgPrice = AuctionManager.INSTANCE.averagePrice(mat);

        protected BuyGui() {
            super("Combien de " + mat + " acheter ?", ItemAuctionGui.this.player, 10, 5, 1, 1);
            this.min = 1;
            this.max = Arrays.stream(player.getInventory().getContents()).mapToInt(s -> {
                if (s == null || s.getType() == Material.AIR)
                    return mat.getMaxStackSize();
                else if (s.getType() == mat)
                    return mat.getMaxStackSize() - s.getAmount();
                else return 0;
            }).sum();
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
                                int remain = finalCount;

                                while (remain > 0) {
                                    int size = Math.min(remain, mat.getMaxStackSize());
                                    ItemStack stack = new ItemStack(mat, size);
                                    player.getInventory().addItem(stack);
                                    remain -= size;
                                }

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

                int remain = quantity;

                while (remain > 0) {
                    int size = Math.min(remain, mat.getMaxStackSize());
                    ItemStack stack = new ItemStack(mat, size);
                    player.getInventory().addItem(stack);
                    remain -= size;
                }

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
