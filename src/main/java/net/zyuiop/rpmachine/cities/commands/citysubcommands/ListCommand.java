package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ListCommand implements SubCommand {
	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "liste les villes";
	}

	@Override
	public boolean run(Player sender, String command, String subCommand, String[] args) {
		sender.sendMessage(ChatColor.GOLD + "-----[ Liste des Villes : ]-----");
		for (City city : RPMachine.getInstance().getCitiesManager().getCities().values()) {
			sender.sendMessage(ChatColor.YELLOW + "- " +city.getCityName() + ", " + ChatColor.GOLD + RPMachine.getInstance().getCitiesManager().getFloor(city).getName() + " " + ChatColor.YELLOW + "de " + ChatColor.GOLD + city.countInhabitants() + " habitants.");
		}
		return true;
	}
}
