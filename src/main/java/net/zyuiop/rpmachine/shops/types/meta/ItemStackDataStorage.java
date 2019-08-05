package net.zyuiop.rpmachine.shops.types.meta;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * @author Louis Vialar
 */
public interface ItemStackDataStorage {
    /**
     * Get the name of the item
     */
    String itemName();

    /**
     * Get the name of the item
     */
    String longItemName();

    /**
     * Add the data to the item stack
     * @param stack the stack to enhance
     * @return the updated stack
     */
    ItemStack createItemStack(ItemStack stack);

    boolean loadFromItemStack(ItemStack stack);

    /**
     * Check if the provided item is the same, making it suitable to refill the shop
     * @param stack
     * @return
     */
    boolean isSameItem(ItemStack stack, Material shopMaterial);
}
