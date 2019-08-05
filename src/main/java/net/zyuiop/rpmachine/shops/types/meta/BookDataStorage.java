package net.zyuiop.rpmachine.shops.types.meta;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Louis Vialar
 */
public class BookDataStorage implements ItemStackDataStorage {
    private @Nullable
    String title;
    private @Nullable
    String author;
    private List<String> pages;

    @Override
    public String itemName() {
        return title == null ? "Livre" : title;
    }

    @Override
    public String longItemName() {
        return itemName() + (author == null ? "" : " par " + author);
    }

    @Override
    public ItemStack createItemStack(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();

        if (meta instanceof BookMeta) {
            BookMeta book = (BookMeta) meta;
            book.setAuthor(author);
            book.setTitle(title);
            book.setGeneration(BookMeta.Generation.COPY_OF_COPY);
            book.setPages(pages);
            stack.setItemMeta(book);
            return stack;
        }
        return null;
    }

    private boolean isIllegalName(String name) {
        name = name.toUpperCase().replaceAll(" ", "_");
        try {
            Material.valueOf(name);
            return true;
        } catch (IllegalArgumentException e) {
            return false; // No material with that name
        }
    }

    @Override
    public boolean loadFromItemStack(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();

        if (stack.getType() == Material.WRITTEN_BOOK && meta instanceof BookMeta) {
            BookMeta book = (BookMeta) meta;

            if (book.getGeneration() == BookMeta.Generation.COPY_OF_COPY || book.getGeneration() == BookMeta.Generation.TATTERED)
                return false; // Cannot sell a copy of copy

            this.author = book.getAuthor();
            this.title = book.getTitle();
            if (isIllegalName(this.title))
                this.title = "Livre " + this.title;
            this.pages = new ArrayList<>(book.getPages());

            return true;
        }
        return false;
    }

    @Override
    public boolean isSameItem(ItemStack stack, Material shopMaterial) {
        return stack.getType() == Material.WRITTEN_BOOK || stack.getType() == Material.WRITABLE_BOOK || stack.getType() == Material.BOOK;
    }
}
