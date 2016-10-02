package net.zyuiop.rpmachine.economy.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandFly implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (! commandSender.hasPermission("rp.fly")) {
			commandSender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
			return true;
		}

		Player player = (Player) commandSender;
		if (!player.getAllowFlight())
			player.setAllowFlight(true);
		else
			player.setAllowFlight(false);
		return true;
	}
}
