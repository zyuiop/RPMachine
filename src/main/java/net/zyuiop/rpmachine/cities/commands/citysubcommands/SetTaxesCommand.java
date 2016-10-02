package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTaxesCommand implements SubCommand {

	private final CitiesManager citiesManager;

	public SetTaxesCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "<montant à payer par block>";
	}

	@Override
	public String getDescription() {
		return "Modifie les taxes dans votre ville. Les taxes s'expriment en $/block : le montant payé par un joueur équivaut à la surface totale de ses terrains multipliée par le montant donné.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Cette ville n'existe pas.");
			} else if (!city.getMayor().equals(player.getUniqueId())) {
				player.sendMessage(ChatColor.RED + "Vous ne pouvez pas gérer les impôts de cette ville.");
			} else if (args.length < 1) {
				player.sendMessage(ChatColor.RED + "Arguments incorrects.");
			} else {
				try {
					Double value = Double.valueOf(args[0]);
					if (value > citiesManager.getFloor(city).getMaxtaxes()) {
						player.sendMessage(ChatColor.RED + "Votre montant est supérieur au montant maximal pour votre palier.");
					}
					city.setTaxes(value);
					citiesManager.saveCity(city);
					player.sendMessage(ChatColor.GREEN + "Les impôts sont désormais de " + value + " $/bloc");
				} catch (Exception e) {
					player.sendMessage(ChatColor.RED + "Le montant est incorrect.");
				}
			}
		}
	}
}
