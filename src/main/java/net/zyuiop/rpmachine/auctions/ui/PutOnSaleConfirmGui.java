package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.SellOrder;
import net.zyuiop.rpmachine.gui.ConfirmGui;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import net.zyuiop.rpmachine.utils.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
class PutOnSaleConfirmGui extends ConfirmGui {
    private final double price;
    private final int quantity;
    private Material mat;

    protected PutOnSaleConfirmGui(Player player, Material mat, double price, int quantity) {
        super("Confirmer vente de " + quantity + " " + mat + " pour " + String.format("%.2f", price * quantity), player);
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
