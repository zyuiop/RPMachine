package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.bridgesapi.api.BukkitBridge;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class SetMayorCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public SetMayorCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "<pseudo>";
	}

	@Override
	public String getDescription() {
		return "Modifie le maire de votre ville.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'êtes membre d'aucune ville.");
			} else if (city.getMayor().equals(player.getUniqueId())) {
				if (args.length < 1) {
					player.sendMessage(ChatColor.RED + "Utilisation incorrecte : /city setmayor " +getUsage());
				} else {
					String newMayor = args[0];
					UUID id = BukkitBridge.get().getUUIDTranslator().getUUID(newMayor, true);
					if (id == null) {
						player.sendMessage(ChatColor.RED + "Ce joueur n'a pas été trouvé.");
					} else {
						if (!city.getInhabitants().contains(id)) {
							player.sendMessage(ChatColor.RED + "Ce joueur n'est pas citoyen de votre ville.");
						} else {
							city.setMayor(id);
							city.getCouncils().add(id);
							player.sendMessage(ChatColor.GREEN + "Le maire a été modifié.");
							citiesManager.saveCity(city);
						}
					}
				}
			} else {
				player.sendMessage(ChatColor.RED + "Seul le maire peut effectuer cette action.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
