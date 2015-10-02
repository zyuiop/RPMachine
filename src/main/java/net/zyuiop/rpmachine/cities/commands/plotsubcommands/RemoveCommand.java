package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.Plot;
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
public class RemoveCommand implements SubCommand {
	private final CitiesManager citiesManager;

	public RemoveCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "<nom>";
	}

	@Override
	public String getDescription() {
		return "Supprime la parcelle <nom>";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas de ville.");
			} else if (!city.getCouncils().contains(player.getUniqueId()) && !player.getUniqueId().equals(city.getMayor())) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de définir des parcelles dans votre ville.");
			} else if (args.length < 1) {
				player.sendMessage(ChatColor.RED + "Argument manquant.");
			} else {
				Plot plot = city.getPlots().get(args[0]);
				if (plot == null)
					player.sendMessage(ChatColor.RED + "Cette parcelle n'existe pas.");
				else {
					city.getPlots().remove(plot.getPlotName());
					citiesManager.saveCity(city);
					player.sendMessage(ChatColor.GREEN + "La parcelle a bien été supprimée.");
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
