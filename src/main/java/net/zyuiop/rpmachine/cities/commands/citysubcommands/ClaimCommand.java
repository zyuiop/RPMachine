package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.CityFloor;
import net.zyuiop.rpmachine.cities.data.VirtualChunk;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public ClaimCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Achète le terrain sur lequel vous vous trouvez pour votre ville.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'êtes membre d'aucune ville. Pour créer une ville, utilisez plutot /createcity");
			} else {
				if (city.getCouncils().contains(player.getUniqueId()) || city.getMayor().equals(player.getUniqueId())) {
					if (!player.getLocation().getWorld().getName().equals("world")) {
						player.sendMessage(ChatColor.RED + "Il est impossible d'agrandir votre ville sur cette carte.");
						return;
					}

					CityFloor floor = citiesManager.getFloor(city);
					Chunk chunk = player.getLocation().getChunk();
					if (citiesManager.getCityHere(chunk) != null) {
						player.sendMessage(ChatColor.RED + "Ce chunk appartient déjà a une ville.");
					} else if (!city.isAdjacent(chunk)) {
						player.sendMessage(ChatColor.RED + "Ce chunk n'est pas adjacent à votre ville.");
					} else if (city.getMoney() < floor.getChunkPrice()) {
						player.sendMessage(ChatColor.RED + "Votre ville ne dispose pas d'assez d'argent pour faire cela. Il lui faut " + floor.getChunkPrice() +" $ au minimum.");
					} else if (city.getChunks().size() >= floor.getMaxsurface()) {
						player.sendMessage(ChatColor.RED + "Votre ville a atteind sa taille maximale.");
					} else if (args.length >= 1 && args[0].equals("confirm")) {
						city.getChunks().add(new VirtualChunk(chunk));
						city.setMoney(city.getMoney() - floor.getChunkPrice());
						citiesManager.saveCity(city);
						player.sendMessage(ChatColor.GREEN + "Votre ville a bien été agrandie sur ce terrain !");
					} else {
						player.sendMessage(ChatColor.GOLD + "Êtes vous certain de vouloir acheter ce chunk ? Cela vous coûtera " + ChatColor.YELLOW + floor.getChunkPrice() + "$");
						player.sendMessage(ChatColor.GOLD + "Tapez " + ChatColor.YELLOW + "/city claim confirm" + ChatColor.GOLD + " pour valider.");
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
