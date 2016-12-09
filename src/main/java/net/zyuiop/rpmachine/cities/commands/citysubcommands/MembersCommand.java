package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
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
		return "<kick <pseudo>|list>";
	}

	@Override
	public String getDescription() {
		return "Affiche la liste des membres de votre ville.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'êtes membre d'aucune ville. Pour créer une ville, utilisez plutot /createcity");
			} else {
				if (args.length == 0 || (args.length > 0 && args[0].equalsIgnoreCase("list"))) {
					player.sendMessage(ChatColor.GOLD + "-----[ Liste des Habitants ]-----");
					for (UUID inhabitant : city.getInhabitants()) {
						String name = RPMachine.database().getUUIDTranslator().getName(inhabitant, false);
						if (name != null)
							player.sendMessage(ChatColor.YELLOW + " - " + name);
					}
				} else if (args.length > 0 && args[0].equalsIgnoreCase("kick")) {
					if (args.length < 2) {
						player.sendMessage(ChatColor.RED + "Le pseudo du joueur est manquant.");
					} else if (city.getMayor().equals(player.getUniqueId())) {
						String pseudo = args[1];
						UUID id = RPMachine.database().getUUIDTranslator().getUUID(pseudo, true);
						if (id == null) {
							player.sendMessage(ChatColor.RED + "Ce joueur n'a pas été trouvé.");
						} else {
							if (!city.getInhabitants().contains(id)) {
								player.sendMessage(ChatColor.RED + "Ce joueur n'est pas membre de votre ville.");
							} else {
								city.getInhabitants().remove(id);
								citiesManager.saveCity(city);
								player.sendMessage(ChatColor.GREEN + "Le joueur a bien été exclus de votre ville.");
							}
						}
					} else {
						player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de faire ça dans cette ville.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "Arguments incorects.");
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}


	}
}
