package net.zyuiop.rpmachine.jobs.restrictions;

import net.zyuiop.rpmachine.jobs.JobRestriction;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;

import java.util.List;
import java.util.Map;

/**
 * @author Louis Vialar
 */
public class EnchantingRestriction extends JobRestriction {

    @EventHandler
    public void onEnchant(EnchantItemEvent enchantItemEvent) {
        if (!isAllowed(enchantItemEvent.getEnchanter()))
            enchantItemEvent.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnchantAnvil(PrepareAnvilEvent event) {
        if (event.getResult() == null || event.getInventory().getItem(0) == null)
            return;

        // Is it an enchantment
        Map<Enchantment, Integer> e = event.getInventory().getItem(0).getEnchantments();

        for (Map.Entry<Enchantment, Integer> entry : event.getResult().getEnchantments().entrySet()) {
            if (!e.containsKey(entry.getKey()) || !e.get(entry.getKey()).equals(entry.getValue())) {
                boolean hasEnchanter = false;
                for (HumanEntity v : event.getInventory().getViewers()) {
                    if (v instanceof Player) {
                        if (isAllowed((Player) v)) {
                            hasEnchanter = true;
                            break;
                        }
                    }
                }

                if (!hasEnchanter)
                    event.setResult(null);

                return;
            }
        }
    }
}
