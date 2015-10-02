package net.zyuiop.rpmachine.economy.commands;

import net.bridgesapi.api.BukkitBridge;
import net.bridgesapi.api.player.PlayerData;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.economy.jobs.Job;
import net.zyuiop.rpmachine.economy.shops.ShopSign;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class CommandJob extends EconomixCommand {
	public CommandJob(RPMachine economix) {
		super(economix);
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (strings.length == 0) {
			commandSender.sendMessage(ChatColor.GOLD + "Un métier définit les types d'items que vous pouvez vendre.");
			commandSender.sendMessage(ChatColor.GOLD + "Listez les métiers disponibles : " + ChatColor.AQUA + "/jobs list");
			commandSender.sendMessage(ChatColor.GOLD + "Informations sur un métier : " + ChatColor.AQUA + "/jobs info <métier>");
			commandSender.sendMessage(ChatColor.GOLD + "Choisissez un métier : " + ChatColor.AQUA + "/jobs choose <métier>");
		} else {
			String com = strings[0];
			if (com.equalsIgnoreCase("list")) {
				commandSender.sendMessage(ChatColor.GOLD + "Voici la liste des métiers que vous pouvez choisir :");
				for (Job job : rpMachine.getJobsManager().getJobs().values())
					commandSender.sendMessage("- " + ChatColor.DARK_AQUA + job.getJobName() + " : " + ChatColor.AQUA + job.getJobDescription());
			} else if (com.equalsIgnoreCase("info")) {
				if (strings.length < 2) {
					commandSender.sendMessage(ChatColor.RED + "Utilisation : /jobs info <métier>");
					return true;
				}

				String job = strings[1];
				Job j = rpMachine.getJobsManager().getJob(job);
				if (j == null) {
					commandSender.sendMessage(ChatColor.RED + "Ce métier n'a pas été trouvé.");
					return true;
				}

				commandSender.sendMessage(ChatColor.GOLD + "Informations sur le métier " + ChatColor.YELLOW + j.getJobName());
				commandSender.sendMessage(ChatColor.GOLD + "Vous pouvez vendre les items suivants :");
				for (Material mat : j.getMaterials())
					commandSender.sendMessage(ChatColor.YELLOW + "- " + mat.toString());

			} else if (com.equalsIgnoreCase("choose")) {
				if (strings.length < 2) {
					commandSender.sendMessage(ChatColor.RED + "Utilisation : /jobs choose <métier>");
					return true;
				}

				String job = strings[1];
				Job j = rpMachine.getJobsManager().getJob(job);
				if (j == null) {
					commandSender.sendMessage(ChatColor.RED + "Ce métier n'a pas été trouvé.");
					return true;
				}

				new Thread(() -> {
					PlayerData data = BukkitBridge.get().getPlayerManager().getPlayerData(((Player) commandSender).getUniqueId());
					if (data.get("job") != null)
						commandSender.sendMessage(ChatColor.RED + "Vous avez déjà un métier.");
					else {
						data.set("job", j.getJobName());
						commandSender.sendMessage(ChatColor.GREEN + "Vous avez bien choisi le métier " + ChatColor.DARK_GREEN + j.getJobName());
					}
				}).start();
			} else if (com.equalsIgnoreCase("quit")) {
				if (strings.length < 2) {
					commandSender.sendMessage(ChatColor.RED + "ATTENTION : L'utilisation de cette commande va déclancher la destruction de *TOUS* vos shops. Merci de confirmer l'opération avec /jobs quit confirm");
					return true;
				} else if (strings[1].equalsIgnoreCase("confirm")) {
					int i = 0;
					for (ShopSign sign : rpMachine.getShopsManager().getPlayerShops(((Player) commandSender).getUniqueId())) {
						sign.breakSign((Player) commandSender);
						i++;
					}
					commandSender.sendMessage(ChatColor.AQUA + "" + i + ChatColor.GOLD + " Shops ont été supprimés.");
					new Thread(() -> {
						PlayerData data = BukkitBridge.get().getPlayerManager().getPlayerData(((Player) commandSender).getUniqueId());
						data.remove("job");
						commandSender.sendMessage(ChatColor.GOLD + "Vous n'avez maintenant plus de métier.");
					}).start();
				}
			}
		}
		return true;
	}
}
