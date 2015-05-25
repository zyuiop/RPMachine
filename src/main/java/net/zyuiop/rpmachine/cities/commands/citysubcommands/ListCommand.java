package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class ListCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public ListCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Affiche la liste des villes du serveur.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.GOLD + "-----[ Liste des Villes : ]-----");
		for (City city : RPMachine.getInstance().getCitiesManager().getCities().values()) {
			sender.sendMessage(ChatColor.YELLOW + "- " +city.getCityName() + ", " + ChatColor.GOLD + RPMachine.getInstance().getCitiesManager().getFloor(city).getName() + " " + ChatColor.YELLOW + "de " + ChatColor.GOLD + city.countInhabitants() + " habitants.");
		}
	}
}
