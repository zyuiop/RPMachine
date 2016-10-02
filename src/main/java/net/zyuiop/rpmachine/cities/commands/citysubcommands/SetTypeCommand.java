package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTypeCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public SetTypeCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "<private|public>";
	}

	@Override
	public String getDescription() {
		return "Modifie le type de votre ville.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'êtes membre d'aucune ville.");
			} else if (city.getMayor().equals(player.getUniqueId())) {
				if (args.length < 1) {
					player.sendMessage(ChatColor.RED + "Utilisation incorrecte : /city settype " +getUsage());
				} else {
					String newType = args[0];
					if (newType.equalsIgnoreCase("private")) {
						city.setRequireInvite(true);
						citiesManager.saveCity(city);
						player.sendMessage(ChatColor.GREEN + "Votre ville est désormais " + ChatColor.RED + "Privée" + ChatColor.GREEN + ". Les joueurs ne pourront la rejoindre qu'avec une invitation.");
					} else if (newType.equalsIgnoreCase("public")) {
						city.setRequireInvite(false);
						citiesManager.saveCity(city);
						player.sendMessage(ChatColor.GREEN + "Votre ville est désormais Publique. Les joueurs pourront la rejoindre librement.");
					} else {
						player.sendMessage(ChatColor.RED + "Type fourni invalide.");
					}
				}
			} else {
				player.sendMessage(ChatColor.RED + "Seul le maire peut effectuer cette action.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
