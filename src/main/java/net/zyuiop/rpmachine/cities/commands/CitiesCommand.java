package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import org.bukkit.command.CommandExecutor;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public abstract class CitiesCommand implements CommandExecutor {

	protected final CitiesManager citiesManager;

	public CitiesCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}
}
