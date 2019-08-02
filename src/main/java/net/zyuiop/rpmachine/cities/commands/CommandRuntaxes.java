package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandRuntaxes extends CitiesCommand {
	public CommandRuntaxes(CitiesManager citiesManager) {
		super(citiesManager);
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		commandSender.sendMessage(ChatColor.GOLD + "Exécution forcée des taxes lancée.");
		citiesManager.payTaxes(true);
		return true;
	}
}
