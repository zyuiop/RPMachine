package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.VirtualChunk;
import net.zyuiop.rpmachine.economy.EconomyManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCityCommand extends CitiesCommand {

	public CreateCityCommand(CitiesManager citiesManager) {
		super(citiesManager);
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (commandSender instanceof Player) {
			Player player = (Player) commandSender;
			if (citiesManager.getPlayerCity(player.getUniqueId()) != null) {
				player.sendMessage(ChatColor.RED + "Vous avez déjà une ville. Merci de la quitter en utilisant " + ChatColor.AQUA + "/city leave");
			} else {
				if (strings.length == 0 || (strings.length >= 1 && strings[0].equals("help"))) {
					player.sendMessage(ChatColor.YELLOW + "/createcity <nom de la ville> <private|public> " + ChatColor.GOLD + "permet de créer une ville sur le chunk où vous vous trouvez.");
					player.sendMessage(ChatColor.GOLD + "private signifie que seules les personnes invitées pourront rejoindre votre ville.");
					player.sendMessage(ChatColor.GOLD + "public signifie que toute personne le souhaitant pourra rejoindre votre ville.");
					player.sendMessage(ChatColor.GOLD + "La création d'une ville coûte actuellement " + ChatColor.YELLOW + citiesManager.getCreationPrice() + " " + EconomyManager.getMoneyName());
					player.sendMessage(ChatColor.RED + "Attention : un nom de ville ne peut contenir d'espaces.");
				} else {
					if (strings.length >= 2) {
						String cityName = strings[0];
						String type = strings[1];
						boolean confirm = (strings.length == 3 && strings[2].equalsIgnoreCase("confirm"));
						if (!cityName.matches("^[a-zA-Z0-9]{3,16}$")) {
							player.sendMessage(ChatColor.RED + "Le nom de votre ville n'est pas valide (3 à 15 caractères alphanumériques)");
						} else if (!type.equalsIgnoreCase("private") && !type.equalsIgnoreCase("public")) {
							player.sendMessage(ChatColor.RED + "Le type de ville ne peut être que PRIVATE ou PUBLIC.");
						} else if (citiesManager.getCityHere(player.getLocation().getChunk()) != null) {
							player.sendMessage(ChatColor.RED + "Il y a déjà une ville sur ce chunk.");
						} else if (citiesManager.getCity(cityName) != null) {
							player.sendMessage(ChatColor.RED + "Une ville de ce nom existe déjà.");
						} else if (!confirm) {
							player.sendMessage(ChatColor.GOLD + "Voulez vous vraiment créer une ville ici ? Cela vous coûtera " + ChatColor.YELLOW + citiesManager.getCreationPrice() + " " + EconomyManager.getMoneyName());
							player.sendMessage(ChatColor.GOLD + "Pour confirmer, tapez /createcity " + cityName + " " + type + " confirm");
						} else {
							RPMachine.getInstance().getEconomyManager().withdrawMoneyWithBalanceCheck(player.getUniqueId(), citiesManager.getCreationPrice(), (newAmount, difference) -> {
								if (difference == 0) {
									player.sendMessage(ChatColor.RED + "Erreur : vous n'avez pas assez d'argent pour cela.");
								} else {
									City city = new City();
									city.setCityName(cityName);
									city.addChunk(new VirtualChunk(player.getLocation().getChunk()));
									city.addInhabitant(player.getUniqueId());
									city.setMayor(player.getUniqueId());
									city.setRequireInvite(type.equalsIgnoreCase("private"));
									city.setTaxes(0.0);
									city.setMayorWage(0.0);
									city.setSpawn(null);
									boolean result = citiesManager.createCity(city);
									if (result) {
										player.sendMessage(ChatColor.GOLD + "Vous créez une ville sur ce chunk.");
									} else {
										RPMachine.getInstance().getEconomyManager().giveMoney(player.getUniqueId(), newAmount);
										player.sendMessage(ChatColor.RED + "Une erreur s'est produite.");
									}
								}
							});
						}
					} else {
						player.sendMessage(ChatColor.RED + "Syntaxe invalide : '/createcity help' pour de l'aide");
					}
				}
			}
		} else {
			commandSender.sendMessage(ChatColor.RED + "Cette commande ne peut être utilisée que par un joueur.");
		}

		return true;
	}
}
