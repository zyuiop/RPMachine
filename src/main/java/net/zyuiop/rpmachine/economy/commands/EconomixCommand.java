package net.zyuiop.rpmachine.economy.commands;

import net.zyuiop.rpmachine.RPMachine;
import org.bukkit.command.CommandExecutor;

@Deprecated
public abstract class EconomixCommand implements CommandExecutor {
	protected final RPMachine rpMachine;

	public EconomixCommand(RPMachine rpMachine) {
		this.rpMachine = rpMachine;
	}
}
