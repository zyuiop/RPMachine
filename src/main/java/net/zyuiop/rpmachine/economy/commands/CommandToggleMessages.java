package net.zyuiop.rpmachine.economy.commands;

import net.bridgesapi.api.BukkitBridge;
import net.bridgesapi.api.player.PlayerData;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
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
public class CommandToggleMessages implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (!(commandSender instanceof Player))
			return false;

		Player player = (Player) commandSender;
		new Thread(() -> {
			PlayerData data = BukkitBridge.get().getPlayerManager().getPlayerData(player.getUniqueId());
			boolean val = !data.getBoolean("seemessages", true);
			data.setBoolean("seemessages", val);
			commandSender.sendMessage(ChatColor.GOLD + "Messages de parcelles : " + ((val ? ChatColor.GREEN + "Activés" : ChatColor.RED + "Désactivés")));
		}).start();
		return true;
	}
}
