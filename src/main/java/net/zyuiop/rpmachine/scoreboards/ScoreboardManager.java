package net.zyuiop.rpmachine.scoreboards;

import net.zyuiop.rpmachine.RPMachine;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreboardManager {
	protected ConcurrentHashMap<UUID, BukkitTask> scoreboards = new ConcurrentHashMap<>();
	private final RPMachine plugin;

	public ScoreboardManager(RPMachine plugin) {
		this.plugin = plugin;
	}

	public void addPlayer(Player player) {
		scoreboards.put(player.getUniqueId(), Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new ScoreboardThread(player), 1L, 1L));
	}

	public void removePlayer(Player player) {
		removePlayer(player.getUniqueId());
	}

	public void removePlayer(UUID uuid) {
		try {
			scoreboards.get(uuid).cancel();
		} catch (Exception ignored) { }
		scoreboards.remove(uuid);
	}
}
