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
abstract class AbstractSetPriceGui extends PickNumberGui {
    private double avgPrice;
    private double minPrice;
    private Material mat;

    protected AbstractSetPriceGui(String title, Player player, Material mat, double avgPrice, double minPrice) {
        super(title, player, 5, .25, .01, avgPrice != avgPrice ? 0D : avgPrice);
        this.avgPrice = avgPrice;
        this.minPrice = minPrice;
        this.min = 0D;
        this.mat = mat;
    }

    @Override
    public final MenuItem updateItem(double value) {
        return new MenuItem(mat).setName(String.format("%.2f", value) + RPMachine.getCurrencyName())
                .setDescription(ChatColor.YELLOW + "Prix moyen " + ChatColor.AQUA + String.format("%.2f", avgPrice) + RPMachine.getCurrencyName(),
                        ChatColor.YELLOW + "Prix minimal " + ChatColor.AQUA + String.format("%.2f", minPrice) + RPMachine.getCurrencyName()
                );
    }

    @Override
    protected final void finish(double value) {
        close();

        next(mat, value);
    }

    protected abstract void next(Material material, double unitPrice);
}
