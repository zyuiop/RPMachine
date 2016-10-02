package net.zyuiop.rpmachine.economy.listeners;

import net.md_5.bungee.api.ChatColor;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
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

		PlayerData d = RPMachine.database().getPlayerData(event.getPlayer().getUniqueId());
		if (d.isNew()) {
			d.setMoney(150D);
			event.setJoinMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "Bienvenue à " + event.getPlayer().getDisplayName() + ChatColor.YELLOW + "" + ChatColor.ITALIC + " !");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLeave(PlayerQuitEvent event) {
		plugin.getScoreboardManager().removePlayer(event.getPlayer());
	}

}
