package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.SellTransaction;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Louis Vialar
 */
public class AutoSellAddItemsGui extends AddItemsGui {
    protected AutoSellAddItemsGui(Player player, Material allowedMaterial) {
        super("Ajouter des items à vendre automatiquement", player, allowedMaterial);
    }

    @Override
    protected void finish(int addedItems) {
        SellTransaction sell = AuctionManager.INSTANCE.startSell(RPMachine.getPlayerActAs(player), mat, addedItems);

        // Creation failed (no buy offers at all)
        if (sell == null) {
            InventoryUtils.giveItems(mat, addedItems, player.getInventory());
            player.saveData();
            player.sendMessage(ChatColor.RED + "Aucune offre d'achat disponible...");
            return;
        }

        // Not enough buy offers
        int diff = addedItems - sell.getTotalAmount();
        if (diff > 0) {
            player.sendMessage(ChatColor.YELLOW + "Trop d'items mis en vente, pas assez d'offres d'achat. Nous vous rendons " + diff + " items.");

            while (diff > 0) {
                int stackSize = Math.min(diff, mat.getMaxStackSize());
                ItemStack stack = new ItemStack(mat, stackSize);
                player.getInventory().addItem(stack);
                diff -= stackSize;
            }
        }

        // Worked - we create the next inventory
        Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(), () -> {
            if (!player.isOnline()) {
                Bukkit.getLogger().info("Player went away, giving back items");
                InventoryUtils.giveItems(mat, sell.getTotalAmount(), player.getInventory());

                player.saveData();
                return;
            }
            Bukkit.getLogger().info("Opening confirm sale window for " + player.getName() + " / " + mat + " / " + addedItems + " total items");

            new TransactionConfirmGui(player, mat, sell).open();
        }, 1);
    }
}
