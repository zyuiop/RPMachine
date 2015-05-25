package net.zyuiop.rpmachine.economy.commands;

import net.zyuiop.rpmachine.RPMachine;
import org.bukkit.command.CommandExecutor;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public abstract class EconomixCommand implements CommandExecutor {
	protected final RPMachine rpMachine;

	public EconomixCommand(RPMachine rpMachine) {
		this.rpMachine = rpMachine;
	}
}
