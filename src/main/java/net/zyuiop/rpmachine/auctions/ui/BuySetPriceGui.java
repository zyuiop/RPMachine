package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.gui.PickNumberGui;
import net.zyuiop.rpmachine.utils.MenuItem;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
class BuySetPriceGui extends PickNumberGui {
    private int quantity;
    private double avgPrice;
    private double minPrice;
    private Material mat;

    protected BuySetPriceGui(Player player, Material mat, int quantity, double avgPrice, double minPrice) {
        super("A quel prix unitaire acheter ?", player, 5, .25, .01, avgPrice != avgPrice ? 0D : avgPrice);
        this.quantity = quantity;
        this.avgPrice = avgPrice;
        this.minPrice = minPrice;
        this.min = 0D;
        this.mat = mat;
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
        new BuyOrderConfirmGui(player, mat, value, quantity).open();
    }
}
