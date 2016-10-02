package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.Plot;
import net.zyuiop.rpmachine.economy.EconomyManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class InfoCommand implements SubCommand {
	private final CitiesManager citiesManager;

	public InfoCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Affiche des informations sur la parcelle dans laquelle vous vous trouvez.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getCityHere(player.getLocation().getChunk());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous ne vous trouvez pas dans une vile.");
			} else {
				Plot plot = city.getPlotHere(player.getLocation());
				if (plot == null) {
					player.sendMessage(ChatColor.RED + "Vous ne vous trouvez pas dans une parcelle.");
				} else {
					player.sendMessage(ChatColor.GOLD + "-----[ Informations Parcelle ]-----");
					player.sendMessage(ChatColor.YELLOW + "Nom : " + plot.getPlotName());
					player.sendMessage(ChatColor.YELLOW + "Ville : " + city.getCityName());
					player.sendMessage(ChatColor.YELLOW + "Surface : " + plot.getArea().getSquareArea() + " blocs");
					player.sendMessage(ChatColor.YELLOW + "Impots : " + plot.getArea().getSquareArea() * city.getTaxes() + " " + EconomyManager.getMoneyName());
					UUID proprio = plot.getOwner();
					if (proprio == null) {
						player.sendMessage(ChatColor.YELLOW + "Propriétaire : " + ChatColor.RED + "Aucun");
					} else {
						String name = RPMachine.database().getUUIDTranslator().getName(proprio, false);
						if (name == null) {
							player.sendMessage(ChatColor.YELLOW + "Propriétaire : " + ChatColor.GOLD + "Inconnu");
						} else {
							player.sendMessage(ChatColor.YELLOW + "Propriétaire : " + ChatColor.GREEN + name);
						}
					}

					ArrayList<String> members = new ArrayList<>();
					for (UUID id : plot.getPlotMembers()) {
						String name = RPMachine.database().getUUIDTranslator().getName(id, false);
						if (name != null)
							members.add(name);
					}

					if (members.size() > 0) {
						String mem = Strings.join(members, ", ");
						player.sendMessage(ChatColor.YELLOW + "Membres : " + mem);
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
