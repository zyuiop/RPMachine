package net.zyuiop.rpmachine.common.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSpawn implements CommandExecutor {

	private final boolean fromCityOnly;

	public CommandSpawn() {
		fromCityOnly = RPMachine.getInstance().getConfig().getBoolean("spawn.fromCityOnly", false);
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (!(commandSender instanceof Player))
			return false;

		Player player = (Player) commandSender;
		if (fromCityOnly && !RPMachine.getInstance().getCitiesManager().checkCityTeleport(player)) {
			return true;
		}

		player.teleport(Bukkit.getWorld("world").getSpawnLocation());
		ReflectionUtils.getVersion().playEndermanTeleport(Bukkit.getWorld("world").getSpawnLocation(), player);

		player.sendMessage(ChatColor.GOLD + "Vous avez été téléporté !");
		return true;
	}
}
