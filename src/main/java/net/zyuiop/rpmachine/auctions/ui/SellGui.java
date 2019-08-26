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
class SellGui extends PickNumberGui {
    private double avgPrice;
    private double minPrice;
    private Material mat;

    protected SellGui(Player player, Material mat, double avgPrice, double minPrice) {
        super("A quel prix unitaire vendre ?", player, 5, .25, .01, avgPrice != avgPrice ? 0D : avgPrice);
        this.avgPrice = avgPrice;
        this.minPrice = minPrice;
        this.min = 0D;
        this.mat = mat;
    }

    @Override
    public MenuItem updateItem(double value) {
        return new MenuItem(mat).setName(String.format("%.2f", value) + RPMachine.getCurrencyName())
                .setDescription(
                        ChatColor.YELLOW + "Prix moyen " + ChatColor.AQUA + String.format("%.2f", avgPrice) + RPMachine.getCurrencyName(),
                        ChatColor.YELLOW + "Prix minimal " + ChatColor.AQUA + String.format("%.2f", minPrice) + RPMachine.getCurrencyName()
                );
    }

    @Override
    protected void finish(double value) {
        player.sendMessage(ChatColor.YELLOW + "Vente au prix unitaire de " + ChatColor.AQUA + String.format("%.2f", value) + RPMachine.getCurrencyName());
        player.sendMessage(ChatColor.YELLOW + "Mettez les items à vendre dans l'inventaire qui va s'ouvrir...");
        player.sendMessage(ChatColor.YELLOW + "Fermez l'inventaire pour valider :=)");

        Bukkit.getLogger().info("Opening SELL inventory for player " + player.getName() + " -- item " + mat + " / price " + value);

        Inventory inventory = Bukkit.createInventory(player, 6 * 9, "Vendre à " + String.format("%.2f", value) + RPMachine.getCurrencyName() + " /unit");

        AuctionInventoryListener.INSTANCE.addPlayer(player, () -> {
            Bukkit.getLogger().info("Closing SELL inventory for player " + player.getName() + " -- item " + mat + " / price " + value);

            int count = 0;
            for (ItemStack stack : inventory.getContents()) {
                if (stack == null) continue;

                if (stack.getType() != mat) {
                    Bukkit.getLogger().info(" --> Putting back stack " + stack.getType() + " x " + stack.getAmount() + " " + stack);

                    // Illegal items go back to their country

                    player.getInventory().addItem(stack);
                } else {
                    // Right material, add to the count
                    count += stack.getAmount();
                    Bukkit.getLogger().info(" --> Adding " + stack.getAmount() + " (count " + count + ")");
                }
            }

            player.closeInventory();

            int finalCount = count;
            Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(), () -> {
                        if (!player.isOnline()) {
                            Bukkit.getLogger().info("Player went away, giving back items");
                            InventoryUtils.giveItems(mat, finalCount, player.getInventory());

                            player.saveData();
                            return;
                        }
                        Bukkit.getLogger().info("Opening confirm sale window for " + player.getName() + " / " + mat + " / " + finalCount + " total items");

                        new PutOnSaleConfirmGui(player, mat, value, finalCount).open();
                    }
                    , 1);
        });

        player.openInventory(inventory);
    }
}
