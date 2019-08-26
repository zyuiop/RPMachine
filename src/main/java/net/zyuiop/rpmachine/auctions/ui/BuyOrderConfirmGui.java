package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.BuyOrder;
import net.zyuiop.rpmachine.gui.ConfirmGui;
import net.zyuiop.rpmachine.utils.MenuItem;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
class BuyOrderConfirmGui extends ConfirmGui {
    private final double price;
    private final int quantity;
    private Material mat;

    protected BuyOrderConfirmGui(Player player, Material mat, double price, int quantity) {
        super("Confirmer achat de " + quantity + " " + mat + " pour " + String.format("%.2f", price * quantity), player);
        this.price = price;
        this.quantity = quantity;
        this.mat = mat;
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
