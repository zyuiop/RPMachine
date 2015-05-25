package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.data.CityFloor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class FloorsCommand extends CitiesCommand {

	public FloorsCommand(CitiesManager citiesManager) {
		super(citiesManager);
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		commandSender.sendMessage(ChatColor.GOLD + " -----[ Paliers de Villes ] -----");
		commandSender.sendMessage(ChatColor.YELLOW + "Voici la liste des paliers disponibles :");
		for (CityFloor floor : citiesManager.getFloors()) {
			commandSender.sendMessage(ChatColor.YELLOW + " - " + ChatColor.GOLD + floor.getName() + ChatColor.YELLOW + ", débloqué à " + ChatColor.GOLD + floor.getInhabitants() + " habitants.");
			commandSender.sendMessage(ChatColor.DARK_AQUA + "Prix par chunk : "+ ChatColor.AQUA + floor.getChunkPrice() + " $");
			commandSender.sendMessage(ChatColor.DARK_AQUA + "Impôt maximal : "+ ChatColor.AQUA + floor.getMaxtaxes() + " $");
			commandSender.sendMessage(ChatColor.DARK_AQUA + "Taille maximale : "+ ChatColor.AQUA + floor.getMaxsurface() + " Chunks");
		}
		return true;
	}
}
