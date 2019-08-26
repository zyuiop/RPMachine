package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionInventoryListener;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.SellTransaction;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Louis Vialar
 */
public abstract class AddItemsGui {
    private final String title;
    protected final Player player;
    protected final Material mat;

    protected AddItemsGui(String title, Player player, Material allowedMaterial) {
        this.title = title;
        this.player = player;
        this.mat = allowedMaterial;
    }

    protected abstract void finish(int addedItems);

    public void open() {
        Inventory inventory = Bukkit.createInventory(player, 6 * 9, title);

        AuctionInventoryListener.INSTANCE.addPlayer(player, () -> {
            Bukkit.getLogger().info("Closing AddItems inventory for player " + player.getName() + " -- item " + mat);

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

            finish(count);
        });

        player.openInventory(inventory);
    }
}
