package net.zyuiop.rpmachine.cities.commands;

import org.bukkit.command.CommandSender;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public interface SubCommand {

	public String getUsage();
	public String getDescription();
	public void run(CommandSender sender, String[] args);
}
