package net.zyuiop.rpmachine.jobs.restrictions;

import com.google.common.collect.ImmutableSet;
import net.zyuiop.rpmachine.jobs.JobRestriction;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Set;

/**
 * @author Louis Vialar
 */
public class KillingRestriction extends JobRestriction {
    private static final Set<EntityType> restricted = ImmutableSet.of(
            EntityType.COW, EntityType.MUSHROOM_COW, EntityType.SHEEP, EntityType.RABBIT, EntityType.PIG, EntityType.CHICKEN
    );

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && restricted.contains(event.getEntityType())) {
            if (!isAllowed((Player) event.getDamager()))
                event.setCancelled(true);
        }
    }
}
