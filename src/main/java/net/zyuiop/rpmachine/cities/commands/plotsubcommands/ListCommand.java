package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.Plot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ListCommand implements SubCommand {
	private final CitiesManager citiesManager;

	public ListCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "[empty|claimed]";
	}

	@Override
	public String getDescription() {
		return "Liste les parcelles dans votre ville.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas de ville.");
			} else {
				boolean claimed = true;
				boolean empty = true;
				if (args.length > 0) {
					if (args[0].equalsIgnoreCase("empty"))
						claimed = false;
					else if (args[0].equalsIgnoreCase("claimed"))
						empty = false;
				}

				player.sendMessage(ChatColor.YELLOW + "-----[ Liste des Parcelles ]-----");
				for (Plot plot : city.getPlots().values()) {
					if (plot.getOwner() == null && empty)
						player.sendMessage(ChatColor.YELLOW + " - " + plot.getPlotName() + ", " + ChatColor.RED + "Aucun proprio.");
					else if (claimed) {
						String prop = RPMachine.database().getUUIDTranslator().getName(plot.getOwner(), false);
						player.sendMessage(ChatColor.YELLOW + " - " + plot.getPlotName() + ", " + ChatColor.GREEN + ((prop == null) ? "Proprio inconnu" : "Proprio : " + prop));
					}
				}

			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
