package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class CityJoinCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public CityJoinCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "<ville>";
	}

	@Override
	public String getDescription() {
		return "Vous permet de rejoindre la ville <ville>. Rejoindre une ville vous permet de participer à des projets au sein de celle ci et d'acheter les parcelles réservées aux citoyens de la ville.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city;
			if (args.length < 1) {
				Chunk chunk = player.getLocation().getChunk();
				city = citiesManager.getCityHere(chunk);
				if (city == null || !chunk.getWorld().getName().equals("world")) {
					player.sendMessage(ChatColor.RED + "Syntaxe incorrecte : /city join <ville>");
					return;
				}
			} else
				city = citiesManager.getCity(args[0]);

			if (city == null) {
				player.sendMessage(ChatColor.RED + "La ville recherchée n'existe pas.");
			} else if (citiesManager.getPlayerCity(player.getUniqueId()) != null) {
				player.sendMessage(ChatColor.RED + "Vous êtes déjà citoyen d'une ville.");
			} else if (city.isRequireInvite() && !city.getInvitedUsers().contains(player.getUniqueId())) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas l'invitation requise pour rejoindre cette ville.");
			} else {
				city.addInhabitant(player.getUniqueId());
				citiesManager.saveCity(city);
				player.sendMessage(ChatColor.GREEN + "Bravo ! Vous êtes désormais citoyen de la ville " + ChatColor.DARK_GREEN + "" + city.getCityName() + "" + ChatColor.GREEN + " !");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
