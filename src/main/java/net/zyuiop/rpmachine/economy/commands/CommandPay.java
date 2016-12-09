package net.zyuiop.rpmachine.economy.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.economy.TaxPayerToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

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
			TaxPayerToken token = RPMachine.getPlayerRoleToken(transactionFrom);
			if (token.getCityName() != null) {
				UUID targetId = target.getUniqueId();
				City city = (City) token.getTaxPayer();
				if (city.getMayor().equals(targetId)) {
					transactionFrom.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous donner d'argent à vous même.");
					return true;
				}
			}

			new Thread(() -> {
				Double val = Double.valueOf(args[1]);
				if (val < 0)
					val = -val;

				rpMachine.getTransactionsHelper().transaction(transactionFrom, token, target, val);
			}).start();
		}

		return true;
	}
}
