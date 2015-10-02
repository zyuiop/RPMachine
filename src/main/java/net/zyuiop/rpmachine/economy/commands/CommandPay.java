package net.zyuiop.rpmachine.economy.commands;

import net.zyuiop.rpmachine.RPMachine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class CommandPay extends EconomixCommand {
	public CommandPay(RPMachine economix) {
		super(economix);
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
		if (args.length < 2) {
			commandSender.sendMessage(ChatColor.RED + "Utilisation : /pay <joueur à payer> <montant à payer>");
			return true;
		}

		Player transactionFrom = (Player) commandSender;
		Player target = Bukkit.getPlayerExact(args[0]);
		if (target == null) {
			commandSender.sendMessage(ChatColor.RED + "Le joueur est actuellement hors ligne.");
			return true;
		} else {
			new Thread(() -> {
				Double val = Double.valueOf(args[1]);
				if (val < 0)
					val = -val;

				rpMachine.getTransactionsHelper().transaction(transactionFrom, target, val);
			}).start();
		}

		rpMachine.getTransactionsHelper().displayAmount((Player) commandSender);
		return true;
	}
}
