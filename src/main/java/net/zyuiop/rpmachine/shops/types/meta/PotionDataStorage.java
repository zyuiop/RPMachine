package net.zyuiop.rpmachine.shops.types.meta;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;

/**
 * @author Louis Vialar
 */
public class PotionDataStorage implements ItemStackDataStorage {
    private PotionData potionType;
    private int color;

    @Override
    public String itemName() {
        String longPrefix = potionType.isExtended() ? "L " : "";
        String upSuffix = potionType.isUpgraded() ? " II" : "";

        return StringUtils.abbreviateMiddle(longPrefix + potionType.getType().name() + upSuffix, "...", 16);
    }

    @Override
    public String longItemName() {
        String longPrefix = potionType.isExtended() ? "Long " : "";
        String upSuffix = potionType.isUpgraded() ? " II" : "";

        return longPrefix + potionType.getType().name() + upSuffix;
    }

    @Override
    public ItemStack createItemStack(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();

        if (meta instanceof PotionMeta) {
            PotionMeta potion = (PotionMeta) meta;
            potion.setBasePotionData(potionType);
            potion.setColor(color == -1 ? null : Color.fromRGB(color));
            stack.setItemMeta(potion);
            return stack;
        }
        return null;
    }

    @Override
    public boolean loadFromItemStack(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();

        if (meta instanceof PotionMeta) {
            PotionMeta potion = (PotionMeta) meta;
            this.potionType = potion.getBasePotionData();
            this.color = potion.getColor() == null ? -1 : potion.getColor().asRGB();

            return true;
        }
        return false;
    }

    @Override
    public boolean isSameItem(ItemStack stack, Material shopMaterial) {
        if (stack.getType() != shopMaterial)
            return false; // only same type of potion

        ItemMeta meta = stack.getItemMeta();

        if (meta instanceof PotionMeta) {
            PotionMeta potion = (PotionMeta) meta;

            return potionType.equals(potion.getBasePotionData()) &&
                    (potion.getColor() == null ? -1 : potion.getColor().asRGB()) == color;
        }
        return false;
    }
}
