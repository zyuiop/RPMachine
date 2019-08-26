package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AbstractTransaction;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.SellTransaction;
import net.zyuiop.rpmachine.gui.ConfirmGui;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import net.zyuiop.rpmachine.utils.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Louis Vialar
 */
class TransactionConfirmGui extends ConfirmGui {
    private final AbstractTransaction transaction;
    private final BukkitTask task;
    private Material mat;

    TransactionConfirmGui(Player player, Material mat, AbstractTransaction transaction) {
        super("Prix total: " + String.format("%.2f", transaction.getPrice()) + ". OK ?", player);
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
        this.mat = mat;
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
