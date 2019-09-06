package net.zyuiop.rpmachine.cities.listeners;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.database.PlayerData;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Sign;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class CitiesListener implements Listener {

    private final CitiesManager manager;

    //private final HashSet<Material> checkInteract;
    public CitiesListener(CitiesManager manager) {
        this.manager = manager;

		/*checkInteract = new HashSet<>();
		checkInteract.add(Material.ACACIA_DOOR);
		checkInteract.add(Material.BIRCH_DOOR);
		checkInteract.add(Material.DARK_OAK_DOOR);
		checkInteract.add(Material.IRON_DOOR);
		checkInteract.add(Material.JUNGLE_DOOR);
		checkInteract.add(Material.WOOD_DOOR);
		checkInteract.add(Material.WOODEN_DOOR);
		checkInteract.add(Material.TRAP_DOOR);
		checkInteract.add(Material.FENCE_GATE);
		checkInteract.add(Material.ACACIA_FENCE_GATE);
		checkInteract.add(Material.BIRCH_FENCE_GATE);
		checkInteract.add(Material.DARK_OAK_FENCE_GATE);
		checkInteract.add(Material.JUNGLE_FENCE_GATE);
		checkInteract.add(Material.CHEST);
		checkInteract.add(Material.TRAPPED_CHEST);
		checkInteract.add(Material.ENDER_CHEST);
		checkInteract.add(Material.STONE_BUTTON);
		checkInteract.add(Material.WOOD_BUTTON);
		checkInteract.add(Material.LEVER);
		checkInteract.add(Material.FURNACE);
		checkInteract.add(Material.BURNING_FURNACE);
		checkInteract.add(Material.HOPPER);
		checkInteract.add(Material.HOPPER_MINECART);
		checkInteract.add(Material.DROPPER);
		checkInteract.add(Material.DISPENSER);
		checkInteract.add(Material.BEACON);*/
    }


    @EventHandler
    public void onGamemode(PlayerGameModeChangeEvent event) {
        if (!event.getPlayer().hasPermission("rp.gamemode") && event.getNewGameMode() != GameMode.SURVIVAL) {
            event.getPlayer().setGameMode(GameMode.SURVIVAL);
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Vous n'avez pas le droit d'accéder au gamemode créatif.");
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        String tag = RPMachine.getPlayerRoleToken(event.getPlayer()).getTag();

        if (!isSameChunk(event.getFrom(), event.getTo())) {
            City c1 = manager.getCityHere(event.getFrom().getChunk());
            City c2 = manager.getCityHere(event.getTo().getChunk());
            boolean entering = false;
            boolean leaving = false;

            if (c1 != null && c2 != null && c1.getCityName().equals(c2.getCityName())) {

                Plot plot = c1.getPlotHere(event.getFrom());
                Plot to = c1.getPlotHere(event.getTo());

                if (plot != null && to != null && plot.getPlotName().equals(to.getPlotName()))
                    return;

                boolean pOverride = (c1.getCouncils().contains(id) || c1.getMayor().equals(id));

                if (plot != null && (tag.equals(plot.getOwner()) || plot.getPlotMembers().contains(id))) {
                    leaving = true;
                } else if (pOverride && plot != null) {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous quittez la parcelle " + plot.getPlotName());
                }

                if (to != null && (tag.equals(to.getOwner()) || to.getPlotMembers().contains(id))) {
                    entering = true;
                } else if (pOverride && to != null) {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous entrez sur la parcelle " + to.getPlotName());
                }

                return;
            }

            if (c1 != null) {
                event.getPlayer().sendMessage(ChatColor.GOLD + "Vous quittez " + ChatColor.YELLOW + c1.getCityName() + ChatColor.GOLD + " !");
                boolean c1Override = (c1.getCouncils().contains(id) || c1.getMayor().equals(id));
                Plot from = c1.getPlotHere(event.getFrom());

                if (from != null && (tag.equals(from.getOwner()) || from.getPlotMembers().contains(id))) {
                    leaving = true;
                } else if (c1Override && from != null) {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous quittez la parcelle " + from.getPlotName());
                }
            }

            if (c2 != null) {
                event.getPlayer().sendMessage(ChatColor.GOLD + "Vous entrez à " + ChatColor.YELLOW + c2.getCityName() + ChatColor.GOLD + " !");

                boolean c2Override = (c2.getCouncils().contains(id) || c2.getMayor().equals(id));
                Plot to = c2.getPlotHere(event.getTo());

                if (to != null && (tag.equals(to.getOwner()) || to.getPlotMembers().contains(id))) {
                    entering = true;
                } else if (c2Override && to != null) {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous entrez sur la parcelle " + to.getPlotName());
                }
            }

            if (entering && !leaving) {
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous entrez sur votre parcelle.");
            } else if (!entering && leaving) {
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous quittez votre parcelle.");
            }
        } else {
            // Same chunk, same city.
            City city = manager.getCityHere(event.getFrom().getChunk());
            Plot plot, to;
            if (city == null) {
                plot = RPMachine.getInstance().getProjectsManager().getZoneHere(event.getFrom());
                to = RPMachine.getInstance().getProjectsManager().getZoneHere(event.getTo());
            } else {
                plot = city.getPlotHere(event.getFrom());
                to = city.getPlotHere(event.getTo());
            }

            boolean pOverride = city == null || (city.getCouncils().contains(id) || city.getMayor().equals(id));
            if (plot != null && to != null && plot.getPlotName().equals(to.getPlotName()))
                return;

            if (plot != null && (tag.equals(plot.getOwner()) || plot.getPlotMembers().contains(id))) {
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous quittez votre parcelle.");
            } else if (pOverride && plot != null) {
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous quittez la parcelle " + plot.getPlotName());
            }

            if (to != null && (tag.equals(to.getOwner()) || to.getPlotMembers().contains(id))) {
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous entrez sur votre parcelle.");
            } else if (pOverride && to != null) {
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Vous entrez sur la parcelle " + to.getPlotName());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        City city = manager.getPlayerCity(event.getPlayer().getUniqueId());
        if (city != null) {
            event.setFormat(city.getChatColor() + "[" + city.getCityName() + "]" + ChatColor.RESET + event.getFormat());
        }
    }

    boolean isSameChunk(Location l1, Location l2) {
        Chunk c1 = l1.getChunk();
        Chunk c2 = l2.getChunk();
        return (c1.getX() == c2.getX() && c1.getZ() == c2.getZ());
    }
}
