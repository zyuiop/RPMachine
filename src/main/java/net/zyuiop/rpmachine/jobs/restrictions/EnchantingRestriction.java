package net.zyuiop.rpmachine.jobs.restrictions;

import net.zyuiop.rpmachine.jobs.JobRestriction;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;

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
        int endSize = event.getResult().getEnchantments().size();
        int startSize = event.getInventory().getItem(0).getEnchantments().size();

        if (endSize > startSize) {
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
        }
    }
}
