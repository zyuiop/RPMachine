package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SimulateTaxesCommand implements SubCommand {
	private final CitiesManager citiesManager;

	public SimulateTaxesCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Simule des impôts et affiche les bénéfices théoriques de ceux ci.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Cette ville n'existe pas.");
			} else if (!city.getMayor().equals(player.getUniqueId()) && !city.getCouncils().contains(player.getUniqueId())) {
				player.sendMessage(ChatColor.RED + "Vous ne pouvez pas gérer les impôts de cette ville.");
			} else {
				player.sendMessage(ChatColor.GOLD + "-----[ Simulation d'impôts ]-----");
				player.sendMessage(ChatColor.YELLOW + "Les impôts actuels de votre ville vous rapportent " + ChatColor.GREEN + "" + city.simulateTaxes() + "$ par semaine.");
			}
		}
	}
}
