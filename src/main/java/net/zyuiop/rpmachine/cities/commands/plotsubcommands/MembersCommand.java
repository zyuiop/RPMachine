package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.bridgesapi.api.BukkitBridge;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.Plot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MembersCommand implements SubCommand {
	private final CitiesManager citiesManager;

	public MembersCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "<ville> <parcelle> <add|remove> <joueur>";
	}

	@Override
	public String getDescription() {
		return "Ajoute ou supprime un membre de votre parcelle.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length < 4) {
				player.sendMessage(ChatColor.RED + "Utilisation : /plot members " + getUsage());
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
			} else if (!city.getCouncils().contains(player.getUniqueId()) && !player.getUniqueId().equals(city.getMayor()) && !player.getUniqueId().equals(plot.getOwner())) {
				player.sendMessage(ChatColor.RED + "Cette parcelle ne vous appartient pas.");
			} else {
				UUID id = BukkitBridge.get().getUUIDTranslator().getUUID(args[3], true);
				if (id == null) {
					player.sendMessage(ChatColor.RED + "Ce joueur n'a pas été trouvé.");
				} else {
					if (args[2].equalsIgnoreCase("add")) {
						if (plot.getPlotMembers().contains(id)) {
							player.sendMessage(ChatColor.GREEN + "Ce joueur est déjà dans la parcelle.");
							return;
						}
						plot.getPlotMembers().add(id);
						citiesManager.saveCity(city);
						player.sendMessage(ChatColor.GREEN + "Le joueur a été ajouté dans la parcelle.");
					} else if (args[2].equalsIgnoreCase("remove")) {
						if (!plot.getPlotMembers().contains(id)) {
							player.sendMessage(ChatColor.GREEN + "Ce joueur n'est pas dans la parcelle.");
							return;
						}
						plot.getPlotMembers().remove(id);
						citiesManager.saveCity(city);
						player.sendMessage(ChatColor.GREEN + "Le joueur a été supprimé de la parcelle.");
					} else {
						player.sendMessage(ChatColor.RED + "Argument invalide (add / remove)");
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
