package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionInventoryListener;
import net.zyuiop.rpmachine.gui.PickNumberGui;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import net.zyuiop.rpmachine.utils.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Louis Vialar
 */
class SellSetPriceGui extends AbstractSetPriceGui {
    protected SellSetPriceGui(Player player, Material mat, double avgPrice, double minPrice) {
        super("A quel prix unitaire vendre ?", player, mat, avgPrice, minPrice);
    }

    @Override
    protected void next(Material mat, double value) {
        player.sendMessage(ChatColor.YELLOW + "Vente au prix unitaire de " + ChatColor.AQUA + String.format("%.2f", value) + RPMachine.getCurrencyName());
        player.sendMessage(ChatColor.YELLOW + "Mettez les items à vendre dans l'inventaire qui va s'ouvrir...");
        player.sendMessage(ChatColor.YELLOW + "Fermez l'inventaire pour valider :=)");

        Bukkit.getLogger().info("Opening SELL inventory for player " + player.getName() + " -- item " + mat + " / price " + value);

        new ManualSellAddItemsGui(player, mat, value).open();
    }
}
