package net.zyuiop.rpmachine.multiverse;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.common.Area;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftWolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.omg.CORBA.Environment;

import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

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

    private MultiversePortal changeDimensionTeleport(PlayerPortalEvent event, World.Environment source, World.Environment target) {
        Location from = event.getFrom().clone();
        Location to = event.getTo().clone();

        if (source == World.Environment.NETHER) {
            // Locations already divided by Minecraft, and Y doesn't seem to change
            String actualWorldName = from.getWorld().getName().replaceAll("_nether", "");
            World actualWorld = Bukkit.getWorld(actualWorldName);

            to.setWorld(actualWorld);
            event.setTo(to);

            RPMachine.getInstance().getLogger().info("Hijack dimension change going from " + from.getWorld().getName() + " to " + actualWorldName);
            // Done here :)
            return null;
        } else if (target == World.Environment.NETHER) {
            MultiverseWorld world = manager.getWorld(from.getWorld().getName());
            if (world == null) {
                RPMachine.getInstance().getLogger().warning("No MultiverseWorld found for " + from.getWorld().getName() + ". Target is nether, letting it pass.");
                return null;
            }

            MultiversePortal portal = world.getPortal(event.getFrom());
            if (portal == null) {
                RPMachine.getInstance().getLogger().info(".. No portal found at " + event.getFrom());

                if (world.isAllowNether()) {
                    RPMachine.getInstance().getLogger().info(".. Rerouting nether teleport");

                    String actualWorldName = from.getWorld().getName() + "_nether";
                    World actualWorld = Bukkit.getWorld(actualWorldName);

                    to.setWorld(actualWorld);
                    event.setTo(to);
                    RPMachine.getInstance().getLogger().info(".. Changed target world to " + actualWorldName);
                    // Done here :)
                } else {
                    RPMachine.getInstance().getLogger().info(".. Nether is not allowed, cancelling");
                    event.setCancelled(true);
                }
                return null;
            }

            return portal;
        }

        return null; // Handle the end in the future
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

            MultiversePortal portal = changeDimensionTeleport(event, current.getEnvironment(), event.getTo().getWorld().getEnvironment());
            if (portal == null)
                return; // Already handled, this is no portal, let it go

            MultiverseWorld target = manager.getWorld(portal.getTargetWorld());
            if (target == null || target.getWorld() == null) {
                event.getPlayer().sendMessage(ChatColor.RED + "Le monde cible n'existe pas...");
                event.setCancelled(true);
                return;
            }

            int playerYDiff = event.getFrom().getBlockY() - portal.getPortalArea().getMinY();

            // Portal detection
            Location opposite = event.getFrom().clone();
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

            event.setCancelled(true);

            if (other == null) {
                if (target.isAllowGeneration()) {
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

                        Area npArea = new Area(first, second);
                        MultiversePortal nPortal = new MultiversePortal(npArea, current.getName());

                        // Clear area around portal
                        Area clearArea = new Area(opposite.getWorld().getName(),
                                opposite.getBlockX() - 5, opposite.getBlockY() - 1, opposite.getBlockZ() - 5,
                                opposite.getBlockX() + 5, opposite.getBlockY() + 10, opposite.getBlockZ() + 5);

                        l.info(".. Clearing area " + clearArea);
                        clearArea.iterator().forEachRemaining(b -> b.setType(Material.AIR));

                        // Build a platform
                        Area platformArea = new Area(opposite.getWorld().getName(),
                                opposite.getBlockX() - 5, opposite.getBlockY() - 1, opposite.getBlockZ() - 5,
                                opposite.getBlockX() + 5, opposite.getBlockY() - 1, opposite.getBlockZ() + 5);

                        l.info(".. Making platform area " + platformArea);
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

                        // Take random blocks from around the portal
                        Location from = event.getFrom().clone();
                        Area overworldArea = new Area(from.getWorld().getName(),
                                from.getBlockX() - 5, from.getBlockY() - 1, from.getBlockZ() - 5,
                                from.getBlockX() + 5, from.getBlockY() + 10, from.getBlockZ() + 5);

                        srcBlocks = overworldArea.iterator();
                        newBlocks = clearArea.iterator();
                        Random rnd = new Random(); // We have 10 * 10 * 10 = 1000 blocks ; would be nice to have 20% of them (up to 200)
                        while (srcBlocks.hasNext() && newBlocks.hasNext()) {
                            Block s = srcBlocks.next();
                            Block t = newBlocks.next();

                            if (npArea.isInside(t.getLocation()))
                                continue; // don't replace portal

                            if (rnd.nextDouble() > 0.2)
                                continue;

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
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "Le portail cible n'existe pas...");
                    manager.deletePortal(portal);
                }
            } else {
                // TP ok, same coords, different world
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Téléportation vers " + target.getWorldName() + " !");

                opposite.setY(opposite.getY() + playerYDiff);
                l.info(".. Changing event target to " + opposite);

                event.getPlayer().teleport(opposite);
            }

        }
    }
}
