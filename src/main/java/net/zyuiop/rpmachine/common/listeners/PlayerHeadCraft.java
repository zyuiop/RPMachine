package net.zyuiop.rpmachine.common.listeners;

import net.zyuiop.rpmachine.RPMachine;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 * @author Louis Vialar
 */
public class PlayerHeadCraft implements Listener {
    private ShapelessRecipe recipe;

    public PlayerHeadCraft() {
        recipe = new ShapelessRecipe(new NamespacedKey(RPMachine.getInstance(), "player_head_craft"), new ItemStack(Material.PLAYER_HEAD));
        recipe.addIngredient(Material.NAME_TAG);
        recipe.addIngredient(new RecipeChoice.MaterialChoice(Material.ZOMBIE_HEAD, Material.CREEPER_HEAD, Material.PLAYER_HEAD, Material.SKELETON_SKULL));

        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onCraft(CraftItemEvent ev) {
        if (ev.getRecipe().getResult().getType() == Material.PLAYER_HEAD) {
            ItemStack stack = null;
            for (ItemStack s : ev.getInventory().getMatrix()) {
                if (s != null && s.getType() == Material.NAME_TAG) {;
                    stack = s;
                    break;
                }
            }

            if (stack == null) {
                return;
            }

            ItemMeta meta = stack.getItemMeta();
            String name = meta.getDisplayName();

            Bukkit.getLogger().info("Creating player head for " + name);

            ItemStack result = ev.getInventory().getResult().clone();
            SkullMeta skull = (SkullMeta) result.getItemMeta();
            skull.setOwningPlayer(Bukkit.getOfflinePlayer(name));
            result.setItemMeta(skull);

            ev.getInventory().setResult(result);
        }
    }
}
