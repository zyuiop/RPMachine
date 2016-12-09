package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.database.FinancialCallback;
import net.zyuiop.rpmachine.economy.TaxPayerToken;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveMoneyCommand implements SubCommand {

	private final CitiesManager citiesManager;

	public GiveMoneyCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "<montant à donner> [nom de la ville]";
	}

	@Override
	public String getDescription() {
		return "Vous permet d'effectuer un don de monnaie à une ville (ou la votre par défaut).";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null && args.length < 2) {
				player.sendMessage(ChatColor.RED + "Vous n'êtes membre d'aucune ville.");
				return;
			} else if (city == null) {
				city = citiesManager.getCity(args[1]);
			}

			if (city == null) {
				player.sendMessage(ChatColor.RED + "Cette ville n'existe pas.");
			} else {
				if (args.length < 1) {
					player.sendMessage(ChatColor.RED + "Utilisation incorrecte : /city givemoney " + getUsage());
				} else {
					String amtStr = args[0];
					try {
						double amt = Double.valueOf(amtStr);
						if (amt <= 0) {
							player.sendMessage(ChatColor.RED + "Montant trop faible.");
						}
						if (!RPMachine.getInstance().getEconomyManager().canPay(player.getUniqueId(), amt)) {
							player.sendMessage(ChatColor.RED + "Vous ne pouvez pas payer ce montant.");
						} else {
							TaxPayerToken token = RPMachine.getPlayerRoleToken(player);

							City finalCity = city;
							RPMachine.getInstance().getEconomyManager().transferMoneyBalanceCheck(token.getTaxPayer(), city, amt, result -> {
								if (result) {
									player.sendMessage(ChatColor.GREEN + "L'argent a bien été transféré.");
								} else {
									player.sendMessage(ChatColor.RED + "La transaction a échoué.");
								}
								citiesManager.saveCity(finalCity);
							});
						}
					} catch (Exception e) {
						player.sendMessage(ChatColor.RED + "Le montant fourni est invalide.");
					}
					citiesManager.saveCity(city);
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
