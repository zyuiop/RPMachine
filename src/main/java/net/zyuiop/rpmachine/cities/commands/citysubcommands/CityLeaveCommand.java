package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class CityLeaveCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public CityLeaveCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Vous permet de quitter la ville dont vous êtes citoyen.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'êtes membre d'aucune ville.");
			} else if (city.getMayor().equals(player.getUniqueId())) {
				player.sendMessage(ChatColor.RED + "Vous ne pouvez quitter une ville dont vous êtes le maire. Si vous souhaitez supprimer votre ville, contactez le staff. Sinon, nommez un autre maire avec /city setmayor <pseudo>");
			} else {
				if (city.getCouncils().contains(player.getUniqueId()))
					city.getCouncils().remove(player.getUniqueId());

				city.getInhabitants().remove(player.getUniqueId());
				city.getInvitedUsers().remove(player.getUniqueId());
				citiesManager.saveCity(city);

				player.sendMessage(ChatColor.RED + "Vous n'êtes plus citoyen de " + city.getCityName());
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
