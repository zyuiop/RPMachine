package net.zyuiop.rpmachine.economy.listeners;

import net.md_5.bungee.api.ChatColor;
import net.bridgesapi.api.BukkitBridge;
import net.bridgesapi.api.player.PlayerData;
import net.zyuiop.rpmachine.RPMachine;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

	private final RPMachine plugin;

	public PlayerListener(RPMachine plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		plugin.getScoreboardManager().addPlayer(event.getPlayer());
		event.setJoinMessage(BukkitBridge.get().getPermissionsManager().getDisplay(BukkitBridge.get().getPermissionsManager().getApi().getUser(event.getPlayer().getUniqueId())) + event.getPlayer().getName() + ChatColor.YELLOW + org.bukkit.ChatColor.ITALIC + " a rejoint le serveur !");

		PlayerData d = BukkitBridge.get().getPlayerManager().getPlayerData(event.getPlayer().getUniqueId());
		if (!d.getKeys().contains("rpmoney")) {
			d.setDouble("rpmoney", 150.0);
			event.setJoinMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "Bienvenue à " + BukkitBridge.get().getPermissionsManager().getDisplay(BukkitBridge.get().getPermissionsManager().getApi().getUser(event.getPlayer().getUniqueId())) + event.getPlayer().getName() + ChatColor.YELLOW + org.bukkit.ChatColor.ITALIC + " !");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLeave(PlayerQuitEvent event) {
		plugin.getScoreboardManager().removePlayer(event.getPlayer());
		event.setQuitMessage(BukkitBridge.get().getPermissionsManager().getDisplay(BukkitBridge.get().getPermissionsManager().getApi().getUser(event.getPlayer().getUniqueId())) + event.getPlayer().getName() + ChatColor.YELLOW + org.bukkit.ChatColor.ITALIC + " s'est déconnecté !");
	}

}
