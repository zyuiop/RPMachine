package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.Plot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements SubCommand {
	private final CitiesManager citiesManager;

	public LeaveCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "<ville> <parcelle>";
	}

	@Override
	public String getDescription() {
		return "Quitte la parcelle choisie.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length < 2) {
				player.sendMessage(ChatColor.RED + "Utilisation : /plot leave " + getUsage());
				return;
			}

			City city = citiesManager.getCity(args[0]);
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Cette ville n'existe pas.");
				return;
			}

			Plot plot = city.getPlots().get(args[1]);
			if (plot == null) {
				player.sendMessage(ChatColor.RED + "Cette parcelle n'existe pas.");
			} else if (!player.getUniqueId().equals(plot.getOwner())) {
				player.sendMessage(ChatColor.RED + "Cette parcelle ne vous appartient pas.");
			} else {
				plot.setOwner(null);
				citiesManager.saveCity(city);
				player.sendMessage(ChatColor.GREEN + "Vous n'êtes plus propriétaire de cette parcelle.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
