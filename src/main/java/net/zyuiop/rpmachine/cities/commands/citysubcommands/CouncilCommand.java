package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.bridgesapi.api.BukkitBridge;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CouncilCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public CouncilCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "<add|remove|list> <pseudo>";
	}

	@Override
	public String getDescription() {
		return "Ajoute ou supprime un adjoint dans votre ville. Pour plus d'informations sur les adjoints, rendez vous sur le forum.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'êtes membre d'aucune ville. Pour créer une ville, utilisez plutot /createcity");
			} else {
				if (city.getMayor().equals(player.getUniqueId())) {
					if (args.length == 1 && args[0].equals("list")) {
						player.sendMessage(ChatColor.GOLD + "-----[ Liste des Conseillers ]-----");
						for (UUID council : city.getCouncils()) {
							String name = BukkitBridge.get().getUUIDTranslator().getName(council, false);
							if (name != null)
								player.sendMessage(ChatColor.YELLOW + " - " + name);
						}
						return;
					} else if (args.length < 2) {
						player.sendMessage(ChatColor.RED + "Arguments incorrects.");
					} else {
						String type = args[0];
						String pseudo = args[1];
						if (!type.equalsIgnoreCase("add") && !type.equalsIgnoreCase("remove")) {
							player.sendMessage(ChatColor.RED + "Arguments incorrects.");
							return;
						}

						UUID id = BukkitBridge.get().getUUIDTranslator().getUUID(pseudo, true);
						if (id == null) {
							player.sendMessage(ChatColor.RED + "Ce joueur n'a pas été trouvé.");
						} else {
							if (type.equalsIgnoreCase("add")) {
								if (!city.getCouncils().contains(id))
									city.getCouncils().add(id);
								player.sendMessage(ChatColor.GREEN + "Ce joueur est désormais conseiller !");
								citiesManager.saveCity(city);
							} else {
								if (city.getCouncils().contains(id))
									city.getCouncils().remove(id);
								player.sendMessage(ChatColor.GREEN + "Ce joueur n'est désormais plus conseiller !");
								citiesManager.saveCity(city);
							}
						}
					}
				} else {
					player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de faire ça dans cette ville.");
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}


	}
}
