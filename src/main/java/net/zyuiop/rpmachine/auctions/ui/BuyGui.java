package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.BuyTransaction;
import net.zyuiop.rpmachine.gui.PickNumberGui;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import net.zyuiop.rpmachine.utils.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
class BuyGui extends PickNumberGui {
    private double avgPrice;
    private boolean autoPrice;
    private Material mat;

    protected BuyGui(Player player, Material mat, boolean autoPrice) {
        super("Combien de " + mat + " acheter ?", player, 10, 5, 1, 1);
        this.autoPrice = autoPrice;
        this.min = 1;
        this.max = autoPrice ? InventoryUtils.availablePlaceFor(player.getInventory(), mat) : Integer.MAX_VALUE;
        this.mat = mat;
        this.avgPrice = AuctionManager.INSTANCE.averagePrice(mat);
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

            new TransactionConfirmGui(player, mat, tx).open();
        } else {
            new BuySetPriceGui(player, mat, (int) value, avgPrice, AuctionManager.INSTANCE.minPrice(mat)).open();

        }
    }
}
