package net.zyuiop.rpmachine.cities;

import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.Selection;
import net.zyuiop.rpmachine.common.VirtualChunk;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SelectionManager implements Listener {
	private ConcurrentHashMap<UUID, Selection> selections = new ConcurrentHashMap<>();
	private final CitiesManager citiesManager;

	public SelectionManager(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getItem() != null && event.getItem().getType().equals(Material.STICK)) {
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null || (!city.getMayor().equals(player.getUniqueId()) && !city.getCouncils().contains(player.getUniqueId())))
				return;

			Selection selection = getSelection(player.getUniqueId());
			if (selection == null)
				selection = new Selection();

			Block block = event.getClickedBlock();
			if (block == null || !block.getWorld().getName().equals("world"))
				return;

			if (!city.getChunks().contains(new VirtualChunk(block.getChunk()))) {
				player.sendMessage(ChatColor.RED + "Ce point est à l'extérieur de votre ville et ne peut donc pas être sélectionné.");
				return;
			}

			Location loc = block.getLocation();

			if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				selection.setLocation2(loc);
				player.sendMessage(ChatColor.GREEN + "Position #2 définie aux coordonnées " + block.getX() +"; "+ block.getY() + "; " + block.getZ());
			} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
				selection.setLocation1(loc);
				player.sendMessage(ChatColor.GREEN + "Position #1 définie aux coordonnées " + block.getX() +"; "+ block.getY() + "; " + block.getZ());
			}

			setSelection(player.getUniqueId(), selection);
			event.setCancelled(true);
		} else if (event.getItem() != null && event.getItem().getType() == Material.BLAZE_ROD) {
			// TODO: merge two code blocks, use same tool for both.
			// TODO: wait actually we might not care as this is admin only
			if (player.hasPermission("zones.select")) {
				Selection selection = getSelection(player.getUniqueId());
				if (selection == null)
					selection = new Selection();

				Block block = event.getClickedBlock();
				if (block == null || !block.getWorld().getName().equals("world"))
					return;

				if (citiesManager.getCityHere(block.getChunk()) != null) {
					player.sendMessage(ChatColor.RED + "Ce point est dans une ville.");
					return;
				}

				Location loc = block.getLocation();

				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					selection.setLocation2(loc);
					player.sendMessage(ChatColor.GREEN + "Position #2 définie aux coordonnées " + block.getX() +"; "+ block.getY() + "; " + block.getZ());
				} else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
					selection.setLocation1(loc);
					player.sendMessage(ChatColor.GREEN + "Position #1 définie aux coordonnées " + block.getX() +"; "+ block.getY() + "; " + block.getZ());
				}

				setSelection(player.getUniqueId(), selection);
				event.setCancelled(true);
			}
		}
	}

	public Selection getSelection(UUID player) {
		return selections.get(player);
	}

	public void setSelection(UUID player, Selection area) {
		selections.put(player, area);
	}

}
