package net.zyuiop.rpmachine.utils;

import com.google.common.collect.Lists;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Louis Vialar
 */
public class MenuItem {
    private ItemStack stack;

    public MenuItem(ItemStack stack) {
        this.stack = new ItemStack(stack);
    }

    public MenuItem(Material material) {
        this(new ItemStack(material));
    }

    public MenuItem(Material material, int qty) {
        this(new ItemStack(material, qty));
    }

    public ItemMeta getItemMeta() {
        return stack.getItemMeta();
    }

    public boolean hasItemMeta() {
        return stack.hasItemMeta();
    }

    public boolean setItemMeta(ItemMeta itemMeta) {
        return stack.setItemMeta(itemMeta);
    }

    public MenuItem setName(String name) {
        ItemMeta meta = getItemMeta();
        meta.setDisplayName(name);
        setItemMeta(meta);
        return this;
    }

    public MenuItem setDescription(String... lines) {
        ItemMeta meta = getItemMeta();
        meta.setLore(Lists.newArrayList(lines));
        setItemMeta(meta);
        return this;
    }

    public MenuItem setDescriptionBlock(String content) {
        return setDescription(content.split("\n"));
    }

    public ItemStack build() {
        return stack;
    }
}
