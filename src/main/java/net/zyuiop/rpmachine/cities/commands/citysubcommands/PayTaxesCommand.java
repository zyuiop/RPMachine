package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.economy.EconomyManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayTaxesCommand implements SubCommand {

	private final CitiesManager citiesManager;

	public PayTaxesCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "<nom de la ville>";
	}

	@Override
	public String getDescription() {
		return "Paye vos impôts en retard à la ville.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length < 1) {
				player.sendMessage(ChatColor.RED + "Utilisation incorrecte : utilisez /city paytaxes " + getUsage());
			} else {
				String cityStr = args[0];
				City city = citiesManager.getCity(cityStr);
				if (city == null) {
					player.sendMessage(ChatColor.RED + "Cette ville n'exite pas.");
					return;
				}
				PlayerData data = RPMachine.database().getPlayerData(player.getUniqueId());
				double topay = data.getUnpaidTaxes(city.getCityName());
				if (topay == 0D) {
					player.sendMessage(ChatColor.GREEN + "Vous ne devez pas d'argent à cette ville.");
				} else {
					double amount = data.getMoney();
					if (amount >= topay) {
						RPMachine.getInstance().getEconomyManager().withdrawMoney(player.getUniqueId(), topay);
						data.setUnpaidTaxes(city.getCityName(), 0D);
						player.sendMessage(ChatColor.GREEN + "Vous ne devez plus rien à cette ville.");
						city.pay(data, topay);
					} else {
						RPMachine.getInstance().getEconomyManager().withdrawMoney(player.getUniqueId(), amount);
						city.setMoney(city.getMoney() + amount);
						topay = topay - amount;
						data.setUnpaidTaxes(city.getCityName(), topay);
						player.sendMessage(ChatColor.RED + "Vous devez encore " + topay + " " + EconomyManager.getMoneyName() + " à la ville.");
						city.pay(data, amount);
					}
					citiesManager.saveCity(city);
				}
			}
		}
	}
}
