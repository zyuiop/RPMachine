package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class CreateCommand implements SubCommand {
	private final CitiesManager citiesManager;

	public CreateCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "<nom> [groundtosky]";
	}

	@Override
	public String getDescription() {
		return "Crée une parcelle sur votre sélection. Si [groundtosky] est spécifiée, la parcelle sera de la couche 0 à 250.";
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
			} else if (RPMachine.getInstance().getSelectionManager().getSelection(player.getUniqueId()) == null) {
				player.sendMessage(ChatColor.RED + "Vous n'avez sélectionné aucune région.");
			} else {
				Selection selection = RPMachine.getInstance().getSelectionManager().getSelection(player.getUniqueId());
				if (selection.getLocation1() == null || selection.getLocation2() == null) {
					player.sendMessage(ChatColor.RED + "Votre sélection n'est pas complète.");
				} else {
					Area area = selection.getArea();
					if (args.length < 1) {
						player.sendMessage(ChatColor.RED + "Syntaxe invalide : /plot create " + getUsage());
					} else {
						String name = args[0];
						if (!name.matches("^[a-zA-Z0-9]{3,16}$")) {
							player.sendMessage(ChatColor.RED + "Le nom de parcelles doit être composé de 3 à 16 caractères alphanumériques.");
							return;
						}

						if (city.getPlots().containsKey(name)) {
							player.sendMessage(ChatColor.RED + "Ce nom de parcelle est déjà utilisé.");
							return;
						}

						if (args.length > 1 && args[1].equalsIgnoreCase("groundtosky")) {
							area.setMax_y(254);
							area.setMin_y(1);
						}

						// Area check //
						int i_x = area.getMin_x();
						while (i_x < area.getMax_x()) {
							int i_z = area.getMin_z();
							while (i_z < area.getMax_z()) {
								if (!city.getChunks().contains(new VirtualChunk(new Location(Bukkit.getWorld("world"), i_x, 64, i_z).getChunk()))) {
									player.sendMessage(ChatColor.RED + "Une partie de votre sélection est hors de la ville.");
									return;
								}

								int i_y = area.getMin_y();
								while (i_y < area.getMax_y()) {
									if (city.getPlotHere(new Location(Bukkit.getWorld("world"), i_x, i_y, i_z)) != null) {
										player.sendMessage(ChatColor.RED + "Une partie de votre sélection fait partie d'une autre parcelle.");
										return;
									}
									i_y ++;
								}
								i_z ++;
							}
							i_x ++;
						}

						Plot plot = new Plot();
						plot.setPlotName(name);
						plot.setArea(area);

						city.getPlots().put(name, plot);
						citiesManager.saveCity(city);

						player.sendMessage(ChatColor.GREEN + "La parcelle a bien été créée.");
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
