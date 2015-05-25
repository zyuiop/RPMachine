package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.bridgesapi.api.player.FinancialCallback;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class GiveMoneyCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public GiveMoneyCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "<montant à donner>";
	}

	@Override
	public String getDescription() {
		return "Vous permet d'effectuer un don de monnaie à votre ville.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'êtes membre d'aucune ville.");
			} else if (args.length < 1) {
				player.sendMessage(ChatColor.RED + "Utilisation incorrecte : /city givemoney <montant>");
			} else {
				String amtStr = args[0];
				try {
					double amt = Double.valueOf(amtStr);
					if (amt <= 0) {
						player.sendMessage(ChatColor.RED + "Montant trop faible.");
					}
					if (! RPMachine.getInstance().getEconomyManager().canPay(player.getUniqueId(), amt)) {
						player.sendMessage(ChatColor.RED + "Vous ne pouvez pas payer ce montant.");
					} else {
						RPMachine.getInstance().getEconomyManager().withdrawMoneyWithBalanceCheck(player.getUniqueId(), amt, new FinancialCallback<Double>() {
							@Override
							public void done(Double newAmount, Double difference, Throwable error) {
								if (difference < 0) {
									city.setMoney(city.getMoney() + (difference * -1));
									citiesManager.saveCity(city);
									player.sendMessage(ChatColor.GREEN + "L'argent a bien été transféré.");
								} else {
									player.sendMessage(ChatColor.RED + "La transaction a échoué.");
								}
							}
						});
					}
				} catch (Exception e) {
					player.sendMessage(ChatColor.RED + "Le montant fourni est invalide.");
				}
				citiesManager.saveCity(city);
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
