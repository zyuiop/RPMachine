package net.zyuiop.rpmachine.common.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Louis Vialar
 */
public class CraftResultAugmenter implements Listener {
    private Map<Material, Integer> increasedAmounts;

    public CraftResultAugmenter(Map<String, Integer> config) {
        increasedAmounts = new HashMap<>();
        config.forEach((key, value) -> {
            increasedAmounts.put(Material.getMaterial(key), value);
            Bukkit.getLogger().info("[CraftResultAugmenter] " + key + " crafts will now loot " + value + " items.");
        });
    }

    @EventHandler
    public void onCraft(CraftItemEvent ev) {
        var resultType = ev.getRecipe().getResult().getType();

        if (this.increasedAmounts.containsKey(resultType)) {
            var result = ev.getInventory().getResult().clone();
            result.setAmount(increasedAmounts.get(resultType));
            ev.getInventory().setResult(result);
        }
    }

    @EventHandler
    public void onPreCraft(PrepareItemCraftEvent ev) {
        if (ev.getRecipe() == null) return;

        var resultType = ev.getRecipe().getResult().getType();

        if (this.increasedAmounts.containsKey(resultType)) {
            var result = ev.getInventory().getResult().clone();
            result.setAmount(increasedAmounts.get(resultType));
            ev.getInventory().setResult(result);
        }
    }
}
