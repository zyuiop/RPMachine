package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public class ManualSellAddItemsGui extends AddItemsGui {
    private final double price;

    protected ManualSellAddItemsGui(Player player, Material allowedMaterial, double price) {
        super("Vendre à " + String.format("%.2f", price) + RPMachine.getCurrencyName() + " /unit", player, allowedMaterial);
        this.price = price;
    }

    @Override
    protected void finish(int addedItems) {
        Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(), () -> {
            if (!player.isOnline()) {
                Bukkit.getLogger().info("Player went away, giving back items");
                InventoryUtils.giveItems(mat, addedItems, player.getInventory());

                player.saveData();
                return;
            }
            Bukkit.getLogger().info("Opening confirm sale window for " + player.getName() + " / " + mat + " / " + addedItems + " total items");

            new PutOnSaleConfirmGui(player, mat, price, addedItems).open();
        }, 1);
    }
}
