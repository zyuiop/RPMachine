package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CityJoinCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public CityJoinCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "<ville>";
	}

	@Override
	public String getDescription() {
		return "rejoint une ville (permet de participer à des projets et d'acheter les parcelles réservées aux citoyens)";
	}

	@Override
	public boolean canUse(Player player) {
		return citiesManager.getPlayerCity(player) == null;
	}

	@Override
	public boolean run(Player player, String[] args) {
		City city;
		if (args.length < 1) {
			Chunk chunk = player.getLocation().getChunk();
			city = citiesManager.getCityHere(chunk);
			if (city == null || !chunk.getWorld().getName().equals("world")) {
				player.sendMessage(ChatColor.RED + "Syntaxe incorrecte : /city join <ville>");
				return false;
			}
		} else {
			city = citiesManager.getCity(args[0]);
		}

		if (city == null) {
			player.sendMessage(ChatColor.RED + "La ville recherchée n'existe pas.");
			return false;
		} else if (city.isRequireInvite() && !city.getInvitedUsers().contains(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "Vous n'avez pas l'invitation requise pour rejoindre cette ville.");
			return false;
		} else {
			city.addInhabitant(player.getUniqueId());
			citiesManager.saveCity(city);
			player.sendMessage(ChatColor.GREEN + "Bravo ! Vous êtes désormais citoyen de la ville " + ChatColor.DARK_GREEN + "" + city.getCityName() + "" + ChatColor.GREEN + " !");
			return true;
		}

		// TODO: émolument pour rejoindre la ville
	}
}
