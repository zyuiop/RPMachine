package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.VirtualChunk;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public SetSpawnCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Définit le spawn de la ville sur votre position.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'êtes membre d'aucune ville.");
			} else if (city.getCouncils().contains(player.getUniqueId()) || city.getMayor().equals(player.getUniqueId())) {
				Location loc = player.getLocation();
				if (!loc.getWorld().getName().equals("world") || !city.getChunks().contains(new VirtualChunk(loc.getChunk()))) {
					player.sendMessage(ChatColor.RED + "Le point de spawn doit se trouver dans votre ville.");
				} else {
					city.setSpawn(new VirtualLocation(loc));
					citiesManager.saveCity(city);
					player.sendMessage(ChatColor.GREEN + "Le spawn de votre ville a été défini !");
				}
			} else {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de faire cela.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
