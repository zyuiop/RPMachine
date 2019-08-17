package net.zyuiop.rpmachine.utils;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * @author Louis Vialar
 */
public class InventoryUtils {

    public static int availablePlaceFor(Inventory inventory, Material mat) {
        return Arrays.stream(inventory.getContents()).mapToInt(s -> {
            if (s == null || s.getType() == Material.AIR)
                return mat.getMaxStackSize();
            else if (s.getType() == mat)
                return mat.getMaxStackSize() - s.getAmount();
            else return 0;
        }).sum();
    }

    public static void giveItems(Material material, int quantity, Inventory inventory) {
        int remain = quantity;

        while (remain > 0) {
            int size = Math.min(remain, material.getMaxStackSize());
            ItemStack stack = new ItemStack(material, size);
            inventory.addItem(stack);
            remain -= size;
        }
    }

}
