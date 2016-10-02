package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.database.FinancialCallback;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.economy.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PayCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public PayCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "<destinataire> <montant>";
	}

	@Override
	public String getDescription() {
		return "Permet de payer un joueur avec le compte de la cité.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'êtes membre d'aucune ville.");
			} else if (args.length < 2) {
				player.sendMessage(ChatColor.RED + "Utilisation incorrecte : /city pay " + getUsage());
			} else if (!city.getMayor().equals(player.getUniqueId())) {
				player.sendMessage(ChatColor.RED + "Cette action ne peut être effectuée que par le maire.");
			} else {
				try {
					String target = args[0];
					UUID targetID = RPMachine.database().getUUIDTranslator().getUUID(target);
					String amtStr = args[1];
					double amount = Double.valueOf(amtStr);
					if (amount < 0)
						amount *= -1;

					if (targetID == null) {
						player.sendMessage(ChatColor.RED + "Ce joueur n'a pas été trouvé.");
					} else if (targetID.equals(player.getUniqueId())) {
						player.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous donner d'argent à vous même.");
					} else if (city.getMoney() < amount) {
						player.sendMessage(ChatColor.RED + "Votre ville ne peut pas payer cette somme.");
					} else {
						city.setMoney(city.getMoney() - amount);
						final double finalAmount = amount;
						RPMachine.getInstance().getEconomyManager().giveMoney(targetID, amount, (newAmount, difference) -> {
							Player tar = Bukkit.getPlayer(targetID);
							if (tar != null) {
								tar.sendMessage(Messages.ECO_PREFIX.getMessage() + ChatColor.YELLOW + "Vous recevez " + ChatColor.AQUA + finalAmount + " " + EconomyManager.getMoneyName() + ChatColor.YELLOW + " de " + ChatColor.AQUA + city.getCityName());
							}

							player.sendMessage(Messages.ECO_PREFIX.getMessage() + ChatColor.YELLOW + "Vous avez envoyé " + ChatColor.GOLD + amtStr + " " + EconomyManager.getMoneyName());
						});
						citiesManager.saveCity(city);
					}
				} catch (Exception e) {
					player.sendMessage(ChatColor.RED + "Le montant est invalide.");
				}

			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
