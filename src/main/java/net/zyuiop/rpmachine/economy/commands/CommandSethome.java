package net.zyuiop.rpmachine.economy.commands;

import net.bridgesapi.api.BukkitBridge;
import net.bridgesapi.api.player.PlayerData;
import net.zyuiop.rpmachine.VirtualLocation;
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
public class CommandSethome implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (!(commandSender instanceof Player))
			return false;

		Player player = (Player) commandSender;
		if (!player.getWorld().getName().equalsIgnoreCase("world")) {
			player.sendMessage(ChatColor.RED + "Vous ne pouvez fixer votre domicile que dans l'overworld.");
			return true;
		}
		VirtualLocation home = new VirtualLocation(player.getLocation());
		new Thread(() -> {
			PlayerData data = BukkitBridge.get().getPlayerManager().getPlayerData(player.getUniqueId());
			data.set("rp.home", home.toString());
			player.sendMessage(ChatColor.GREEN + "Votre domicile a bien été fixé !");
		}).start();
		return true;
	}
}
