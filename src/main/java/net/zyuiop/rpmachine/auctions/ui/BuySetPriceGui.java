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
class BuySetPriceGui extends AbstractSetPriceGui {
    private final int quantity;

    protected BuySetPriceGui(Player player, Material mat, int quantity, double avgPrice, double minPrice) {
        super("A quel prix unitaire acheter ?", player, mat, avgPrice, minPrice);
        this.quantity = quantity;
    }

    @Override
    protected void next(Material material, double unitPrice) {
        double price = unitPrice * quantity;

        if (RPMachine.getPlayerActAs(player).getBalance() < price) {
            Messages.notEnoughMoneyEntity(player, RPMachine.getPlayerActAs(player), price);
            return;
        }
        new BuyOrderConfirmGui(player, material, value, quantity).open();
    }
}
