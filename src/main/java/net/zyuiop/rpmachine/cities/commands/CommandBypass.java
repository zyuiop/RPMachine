package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CitiesCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBypass extends CitiesCommand {
	public CommandBypass(CitiesManager citiesManager) {
		super(citiesManager);
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		boolean isBypassing = citiesManager.isBypassing(((Player) commandSender).getUniqueId());
		if (isBypassing) {
			commandSender.sendMessage(ChatColor.GREEN + "Bypass Mode désactivé.");
			citiesManager.removeBypass(((Player) commandSender).getUniqueId());
		} else {
			commandSender.sendMessage(ChatColor.GREEN + "Bypass Mode activé.");
			citiesManager.addBypass(((Player) commandSender).getUniqueId());
		}
		return true;
	}
}
