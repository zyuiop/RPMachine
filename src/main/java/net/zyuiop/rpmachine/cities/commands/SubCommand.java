package net.zyuiop.rpmachine.cities.commands;

import org.bukkit.command.CommandSender;

public interface SubCommand {

	public String getUsage();
	public String getDescription();
	public void run(CommandSender sender, String[] args);
}
