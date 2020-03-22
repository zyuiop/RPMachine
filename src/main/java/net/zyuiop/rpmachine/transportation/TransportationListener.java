package net.zyuiop.rpmachine.transportation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.Arrays;

public class TransportationListener implements Listener {
    private final TransportationManager manager;

    public TransportationListener(TransportationManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onLoad(ChunkLoadEvent ev) {
        Arrays.stream(ev.getChunk().getEntities())
                .filter(e -> e.isInvulnerable() && e.getType() != EntityType.VILLAGER && e instanceof Mob && e.getCustomName() != null)
                .forEach(e -> {
                    Bukkit.getLogger().info("[Transportation] Chunk load: removing invuln. entity" + e + " at " + e.getLocation());
                    e.remove();
                });
    }

    @EventHandler
    public void onDismount(EntityDismountEvent ev) {
        if (ev.getEntity() instanceof Player && ev.isCancellable()) {
            var p = (Player) ev.getEntity();
            var ti = TransportationPathInstance.getCurrentTransportation(p);

            if (ti != null) {
                p.sendMessage(ChatColor.GRAY + "Votre voyage n'est pas terminé...");
                ev.setCancelled(true); // Cannot dismount carrier
            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent ev) {
        var ti = TransportationPathInstance.getCurrentTransportation(ev.getPlayer());

        if (ti != null) {
            ti.cancel();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        var ti = TransportationPathInstance.getCurrentTransportation(ev.getPlayer());

        if (ti != null) {
            ti.cancel();
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEntityEvent event) {
        var cancel = this.interact(event.getRightClicked(), event.getPlayer());

        if (cancel && !event.isCancelled()) event.setCancelled(true);
    }


    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractAtEntityEvent event) {
        var cancel = this.interact(event.getRightClicked(), event.getPlayer());

        if (cancel && !event.isCancelled()) event.setCancelled(true);
    }

    private boolean interact(Entity e, Player player) {
        if (e.getType() == EntityType.VILLAGER) {
            var villager = (Villager) e;
            if (e.isInvulnerable() && !villager.isAware() && !villager.hasAI()) {
                // Ofc a plugin spawned villager
                TransportationNPC npc = manager.getNpc(e.getLocation());

                if (npc != null) {
                    npc.onClick(player);
                }

                return true;
            }
        }

        return false;
    }
}
