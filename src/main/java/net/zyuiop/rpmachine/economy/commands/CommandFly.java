package net.zyuiop.rpmachine.economy.commands;


import net.bridgesapi.api.BukkitBridge;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class CommandFly implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (! BukkitBridge.get().getPermissionsManager().hasPermission(commandSender, "rp.fly")) {
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
