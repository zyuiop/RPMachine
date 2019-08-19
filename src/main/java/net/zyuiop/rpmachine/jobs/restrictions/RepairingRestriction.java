package net.zyuiop.rpmachine.jobs.restrictions;

import net.zyuiop.rpmachine.jobs.JobRestriction;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.meta.Damageable;

/**
 * Restricts repairing through an anvil, but not through classic inventory, as it doesn't give a high boost and consumes enchantme
 * @author Louis Vialar
 */
public class RepairingRestriction extends JobRestriction {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRepairAnvil(PrepareAnvilEvent event) {
        if (event.getResult() == null || event.getInventory().getItem(0) == null)
            return;

        if (event.getResult().getData() instanceof Damageable && event.getInventory().getItem(0).getData() instanceof Damageable) {
            Damageable source = (Damageable) event.getInventory().getItem(0).getData();
            Damageable target = (Damageable) event.getResult().getData();

            if (target.getDamage() < source.getDamage()) {
                boolean isAllowed = false;
                for (HumanEntity v : event.getInventory().getViewers()) {
                    if (v instanceof Player) {
                        if (isAllowed((Player) v)) {
                            isAllowed = true;
                            break;
                        }
                    }
                }

                if (!isAllowed)
                    event.setResult(null);
            }
        }
    }
}
