package net.zyuiop.rpmachine.multiverse;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.common.Area;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Iterator;
import java.util.logging.Logger;

/**
 * @author Louis Vialar
 */
public class MultiverseListener implements Listener {
    private final MultiverseManager manager;

    public MultiverseListener(MultiverseManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChangeWorld(PlayerPortalEvent event) {
        Logger l = RPMachine.getInstance().getLogger();
        l.info(event.getPlayer() + " changes world " + event.getCause());
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            // Hijack!

            // Where are we
            World current = event.getFrom().getWorld();

            if (current == null || current.getEnvironment() != World.Environment.NORMAL) {
                l.info(".. Abort hijack: current world is " + (current == null ? "null" : current.getEnvironment()));
                return; // don't care
            }

            // Overworld?
            MultiverseWorld world = manager.getWorld(current.getName());
            if (world == null) {
                RPMachine.getInstance().getLogger().warning("No MultiverseWorld found for " + current.getName());
                event.setCancelled(true);
                return;
            }

            MultiversePortal portal = world.getPortal(event.getFrom());
            if (portal == null) {
                l.info(".. No portal found at " + event.getFrom());
                event.setCancelled(!world.isAllowNether());
                return;
            }

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

            main: for (int x = -3; x < 3; ++x) {
                for (int y = -3; y < 3; ++y) {
                    for (int z = -3; z < 3; ++z) {
                        Location loc = opposite.clone().add(x, y, z);
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
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Patientez, création du portail en cours...");
                    l.info(".. Creating sibling portal at " + opposite + " for " + portal.getPortalArea());
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

                    // Register the portal
                    manager.createPortal(nPortal);

                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "Le portail cible n'existe pas...");
                    manager.deletePortal(portal);
                }
            }
            // TP ok, same coords, different world
            event.getPlayer().sendMessage(ChatColor.YELLOW + "Téléportation vers " + target.getWorldName() + " !");

            opposite.setY(opposite.getY() + playerYDiff);
            l.info(".. Changing event target to " + opposite);

            event.getPlayer().teleport(opposite);

        }
    }
}
