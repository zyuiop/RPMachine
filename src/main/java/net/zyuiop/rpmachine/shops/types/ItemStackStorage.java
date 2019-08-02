package net.zyuiop.rpmachine.shops.types;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * @author Louis Vialar
 */
public class ItemStackStorage {
    private static final Map<Material, Class<? extends ItemStackDataStorage>> DATAS = ImmutableMap.of(
            Material.POTION, PotionDataStorage.class,
            Material.LINGERING_POTION, PotionDataStorage.class,
            Material.SPLASH_POTION, PotionDataStorage.class,
            Material.FIREWORK_ROCKET, FireworkDataStorage.class
    );

    private Material itemType;
    private ItemStackDataStorage dataStorage;

    public ItemStackStorage(Material itemType, ItemStackDataStorage dataStorage) {
        this.itemType = itemType;
        this.dataStorage = dataStorage;
    }

    public static ItemStackStorage init(ItemStack stack) {
        // Data storage
        Class<? extends ItemStackDataStorage> storageClass = DATAS.get(stack.getType());
        ItemStackDataStorage storage = null;
        if (storageClass != null) {
            try {
                storage = storageClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }

            if (!storage.loadFromItemStack(stack)) {
                return null;
            }
        }

        return new ItemStackStorage(stack.getType(), storage);
    }

    public ItemStackStorage() {
    }

    public Material getItemType() {
        return itemType;
    }

    public ItemStackDataStorage getDataStorage() {
        return dataStorage;
    }

    public String itemName() {
        if (dataStorage != null)
            return dataStorage.itemName();
        return itemType.name();
    }


    public String longItemName() {
        if (dataStorage != null)
            return itemType.name() + " " + dataStorage.longItemName();
        return itemType.name();
    }

    public ItemStack createItemStack(int amount) {
        ItemStack stack = new ItemStack(itemType, amount);
        if (dataStorage != null)
            return dataStorage.createItemStack(stack);
        return stack;
    }

    public boolean isItemValid(ItemStack stack) {
        if (stack == null) return false;

        if (stack.getType() != itemType) return false;

        if (dataStorage != null) return dataStorage.isSameItem(stack);

        return true;
    }

    public int maxAmount() {
        return itemType.getMaxStackSize();
    }
}
