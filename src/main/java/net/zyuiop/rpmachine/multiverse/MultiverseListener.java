package net.zyuiop.rpmachine.multiverse;

import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.common.regions.RectangleRegion;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Louis Vialar
 */
public class MultiverseListener implements Listener {
    private final MultiverseManager manager;

    public MultiverseListener(MultiverseManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        Location loc = ev.getPlayer().getLocation();
        if (loc.getWorld().getName().equalsIgnoreCase("world")) {
            if (!loc.getBlock().isEmpty() && !loc.getBlock().isLiquid()) {
                ev.getPlayer().teleport(loc.getWorld().getHighestBlockAt(loc).getLocation().add(0, 1, 0));
            }
        }
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent ev) {
        World current = ev.getBlock().getWorld();
        MultiverseWorld world = manager.getWorld(current.getName());

        if (world != null) {
            MultiversePortal portal = world.getPortal(ev.getBlock().getLocation());

            if (portal != null) {
                if (ev.getPlayer().hasPermission("admin.breakportal")) {
                    ev.getPlayer().sendMessage(ChatColor.RED + "Portail supprimé.");
                    manager.deletePortal(portal);
                } else {
                    ev.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent ev) {
        World current = ev.getBlock().getWorld();
        MultiverseWorld world = manager.getWorld(current.getName());
        if (world != null) {
            MultiversePortal portal = world.getPortal(ev.getBlock().getLocation());
            if (portal != null) {
                if (!ev.getPlayer().hasPermission("admin.breakportal")) {
                    ev.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChangeWorld(EntityPortalEvent event) {
        if (event.getEntity() instanceof Player)
            return;

        Logger l = RPMachine.getInstance().getLogger();
        l.info(event.getEntity() + " changes world");

        // Where are we
        World current = event.getFrom().getWorld();
        if (current == null) {
            l.info(".. Abort hijack: current world is null");
            return; // don't care
        }

        try {
            Location target = changeDimensionTeleport(event.getFrom(), event.getTo(), event.getEntity(), null, current.getEnvironment(), event.getTo().getWorld().getEnvironment());

            if (target == null)
                event.setCancelled(true);
            else
                event.setTo(target);
        } catch (MultiverseException e) {
            event.setCancelled(true);
            l.warning("Exception while changing dimension : " + e.getMessage());
        }

    }


    private Location changeDimensionTeleport(Location from, Location to, Entity teleporting, @Nullable PlayerPortalEvent event, World.Environment source, World.Environment target) throws MultiverseException {
        if (source == World.Environment.NETHER) {
            return rerouteNetherReturn(from, to);
        } else if (target == World.Environment.NETHER) {
            MultiverseWorld world = manager.getWorld(from.getWorld().getName());
            if (world == null) {
                RPMachine.getInstance().getLogger().warning("No MultiverseWorld found for " + from.getWorld().getName() + ". Target is nether, letting it pass.");
                return to;
            }

            MultiversePortal portal = world.getPortal(from);
            if (portal == null) {
                Location redirect = rerouteNether(world, from, to);
                if (redirect != null) {
                    return redirect;
                } else {
                    throw new MultiverseException("Le nether n'est pas autorisé depuis ce monde");
                }
            } else {
                try {
                    Location tp = rerouteMultiverse(portal, from, to, event);

                    if (tp != null) {
                        RPMachine.getInstance().getLogger().info(".. Changing event target to " + tp);

                        if (event != null)
                            event.getPlayer().sendMessage(ChatColor.YELLOW + "Téléportation vers " + tp.getWorld().getName() + " !");
                        teleporting.teleport(tp);
                    }

                    return null;
                } catch (MultiverseException e) {
                    manager.deletePortal(portal);
                    throw e; // Rethrow after deleting portal
                }
            }
        } else {
            return null;
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPigmanSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NETHER_PORTAL) {
            Location from = event.getLocation();

            MultiverseWorld world = manager.getWorld(from.getWorld().getName());
            if (world == null) {
                return;
            }

            MultiversePortal portal = world.getPortal(from);
            if (portal != null) {
                RPMachine.getInstance().getLogger().info("Cancelling creature spawn due to Nether Portal");
                event.setCancelled(true);
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChangeWorld(PlayerPortalEvent event) {
        Logger l = RPMachine.getInstance().getLogger();
        l.info(event.getPlayer() + " changes world " + event.getCause());
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            // Hijack!

            // Where are we
            World current = event.getFrom().getWorld();

            if (current == null) {
                l.info(".. Abort hijack: current world is null");
                return; // don't care
            }

            try {
                Location target = changeDimensionTeleport(event.getFrom(), event.getTo(), event.getPlayer(), event, current.getEnvironment(), event.getTo().getWorld().getEnvironment());

                if (target == null)
                    event.setCancelled(true);
                else
                    event.setTo(target);
            } catch (MultiverseException e) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + e.getMessage());
            }
        } else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            if (event.getTo().getWorld().getEnvironment() == World.Environment.THE_END) {
                // TODO: test this piece
                l.info(".. target is end, rerouting accordingly");

                MultiverseWorld world = manager.getWorld(event.getFrom().getWorld().getName());
                if (world == null) {
                    l.info(".. world is not registerd, ignoring");
                    return; // No multiverse world
                }

                if (world.isAllowEnd()) {
                    String actualWorldName = event.getFrom().getWorld().getName() + "_the_end";
                    World actualWorld = Bukkit.getWorld(actualWorldName);
                    event.setTo(actualWorld.getSpawnLocation());
                    RPMachine.getInstance().getLogger().info(".. Changed target world to " + actualWorldName);
                } else {
                    RPMachine.getInstance().getLogger().info(".. End is not allowed, cancelling");
                    event.getPlayer().sendMessage(ChatColor.RED + "L'end n'est pas autorisé depuis ce monde.");
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Find the new location for a player entering a portal from the nether
     *
     * @param from the source location
     * @param to   the current target location
     * @return the new target location
     */
    private Location rerouteNetherReturn(Location from, Location to) {
        String actualWorldName = from.getWorld().getName().replaceAll("_nether", "");
        World actualWorld = Bukkit.getWorld(actualWorldName);

        to.setWorld(actualWorld);

        RPMachine.getInstance().getLogger().info("Hijack dimension change going from " + from.getWorld().getName() + " to " + actualWorldName);
        return to;
    }

    /**
     * Find the new location for a player entering a portal targeting the nether that is not a MV portal
     *
     * @param from the source location
     * @param to   the current target location
     * @return the new target location
     */
    private Location rerouteNether(MultiverseWorld currentWorld, Location from, Location to) {
        if (currentWorld.isAllowNether()) {
            RPMachine.getInstance().getLogger().info(".. Rerouting nether teleport");

            String actualWorldName = from.getWorld().getName() + "_nether";
            World actualWorld = Bukkit.getWorld(actualWorldName);

            to.setWorld(actualWorld);
            RPMachine.getInstance().getLogger().info(".. Changed target world to " + actualWorldName);
            return to;
            // Done here :)
        } else {
            RPMachine.getInstance().getLogger().info(".. Nether is not allowed, cancelling");
            return null;
        }
    }

    private Location rerouteMultiverse(MultiversePortal portal, Location from, Location to, @Nullable PlayerPortalEvent event) throws MultiverseException {
        MultiverseWorld target = manager.getWorld(portal.getTargetWorld());
        if (target == null || target.getWorld() == null) {
            throw new MultiverseException("Le monde cible n'existe pas.");
        }

        int playerYDiff = from.getBlockY() - portal.getPortalArea().getMinY();

        // Portal detection
        Location opposite = from.clone();
        opposite.setWorld(target.getWorld());
        opposite.setY(target.getWorld().getHighestBlockYAt(opposite));
        MultiversePortal other = null;

        main:
        for (int x = -3; x < 3; ++x) {
            for (int y = 0; y < 250; ++y) {
                for (int z = -3; z < 3; ++z) {
                    Location loc = opposite.clone().add(x, 0, z);
                    loc.setY(y);
                    other = target.getPortal(loc);

                    if (other != null) {
                        opposite.setY(other.getPortalArea().getMinY());
                        break main;
                    }
                }
            }
        }

        if (other == null) {
            if (target.isAllowGeneration() && event != null) {
                generatePortal(event, opposite, portal, from.getWorld(), target, playerYDiff);
                return null;
            } else {
                throw new MultiverseException("Le portail cible n'existe pas...");
            }
        } else {
            // TP ok, same coords, different world

            opposite.setY(opposite.getY() + playerYDiff);
            return opposite;
        }

    }

    private void generatePortal(PlayerPortalEvent event, Location opposite, MultiversePortal portal, World current, MultiverseWorld target, int playerYDiff) {
        Logger l = RPMachine.getInstance().getLogger();
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Création d'un portail en monde " + ChatColor.GOLD + target.getWorldName() + ChatColor.YELLOW + ", risque de lag.");
        event.getPlayer().sendMessage(ChatColor.YELLOW + "Patientez, création du portail en cours...");
        l.info(".. Creating sibling portal at " + opposite + " for " + portal.getPortalArea());
        l.info(".. Saving");
        Bukkit.savePlayers();
        Bukkit.getWorld("world").save();
        l.info(".. Done");

        Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(), () -> {
            l.info(".. Building portal");
            // Generate target portal
            Location first = portal.getPortalArea().getFirst();
            Location second = portal.getPortalArea().getSecond();

            first.setY(opposite.getY());
            first.setWorld(target.getWorld());

            second.setY(first.getY() + (portal.getPortalArea().getMaxY() - portal.getPortalArea().getMinY()));
            second.setWorld(target.getWorld());

            RectangleRegion npArea = new RectangleRegion(first, second);
            MultiversePortal nPortal = new MultiversePortal(npArea, current.getName());

            // Clear area around portal
            RectangleRegion clearArea = new RectangleRegion(opposite.getWorld().getName(),
                    first.getBlockX() - 5, first.getBlockY() - 1, first.getBlockZ() - 5,
                    second.getBlockX() + 5, second.getBlockY() + 5, second.getBlockZ() + 5);

            l.info(".. Clearing area " + clearArea);
            clearArea.iterator().forEachRemaining(b -> b.setType(Material.AIR));

            // Find platform material
            Location from = event.getFrom().clone();
            RectangleRegion overworldArea = new RectangleRegion(from.getWorld().getName(),
                    from.getBlockX() - 25, from.getBlockY() - 25, from.getBlockZ() - 25,
                    from.getBlockX() + 25, from.getBlockY() + 25, from.getBlockZ() + 25);


            var materials = StreamSupport.stream(overworldArea.spliterator(), false)
                    .filter(b -> !b.isEmpty() && !b.isLiquid())
                    .map(Block::getType)
                    .collect(Collectors.toList());
            Collections.shuffle(materials);

            // Build a platform


            RectangleRegion portalFloorArea = new RectangleRegion(opposite.getWorld().getName(),
                    first.getBlockX() - 5, first.getBlockY() - 1, first.getBlockZ() - 5,
                    second.getBlockX() + 5, first.getBlockY() -1, second.getBlockZ() + 5);
            RectangleRegion platformArea = portalFloorArea.shift(0, -1, 0);

            l.info(".. Making platform area " + platformArea);

            var materialIterator = materials.iterator();
            portalFloorArea.iterator().forEachRemaining(block -> {
                if (materialIterator.hasNext()) {
                    block.setType(materialIterator.next());
                } else {
                    block.setType(Material.BEDROCK);
                }
            });

            platformArea.iterator().forEachRemaining(b -> b.setType(Material.BEDROCK));

            // Clone the portal
            Iterator<Block> srcBlocks = portal.getPortalArea().iterator();
            Iterator<Block> newBlocks = nPortal.getPortalArea().iterator();

            l.info(".. Creating portal area " + nPortal.getPortalArea());

            while (srcBlocks.hasNext() && newBlocks.hasNext()) {
                Block s = srcBlocks.next();
                Block t = newBlocks.next();

                t.setType(s.getType(), false);
                t.setBlockData(s.getBlockData().clone(), false);
            }

            // Register the portal
            manager.createPortal(nPortal);

            // Do teleport
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Téléportation vers " + target.getWorldName() + " !");

            opposite.setY(opposite.getY() + playerYDiff);
            l.info(".. Changing event target to " + opposite);

            event.getPlayer().teleport(opposite);
        }, 5L);
    }

    private static class MultiverseException extends Exception {
        public MultiverseException(String message) {
            super(message);
        }
    }


}
