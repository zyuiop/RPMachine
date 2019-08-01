package net.zyuiop.rpmachine.jobs.restrictions;

import net.zyuiop.rpmachine.jobs.JobRestriction;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;

/**
 * @author Louis Vialar
 */
public class BreedingRestriction extends JobRestriction {
    @EventHandler
    public void onUseShear(PlayerShearEntityEvent entityEvent) {
        if (!isAllowed(entityEvent.getPlayer()))
            entityEvent.setCancelled(true);
    }

    @EventHandler
    public void onBreed(EntityBreedEvent entityBreedEvent) {
        if (entityBreedEvent.getBreeder() != null && entityBreedEvent.getBreeder() instanceof Player) {
            if (!(isAllowed((Player) entityBreedEvent.getBreeder()))) {
                entityBreedEvent.setCancelled(true);

                ((Animals) entityBreedEvent.getFather()).setLoveModeTicks(0);
                ((Animals) entityBreedEvent.getMother()).setLoveModeTicks(0);
            }
        }
    }
}
