package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import org.bukkit.command.CommandExecutor;

public abstract class CitiesCommand implements CommandExecutor {

	protected final CitiesManager citiesManager;

	public CitiesCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}
}
