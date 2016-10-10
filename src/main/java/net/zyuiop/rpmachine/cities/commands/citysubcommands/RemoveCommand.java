package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.economy.shops.AbstractShopSign;
import net.zyuiop.rpmachine.economy.shops.ItemShopSign;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public RemoveCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Supprime votre ville actuelle.";
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
					if (args.length >= 1 && args[0].equals("confirm")) {
						player.sendMessage(ChatColor.RED + "Suppression de la ville : suppression des chunks (1/2)");
						for (VirtualChunk chunk : city.getChunks()) {
							Bukkit.getWorld("world").regenerateChunk(chunk.getX(), chunk.getZ());
						}
						player.sendMessage(ChatColor.RED + "Suppression de la configuration");
						citiesManager.removeCity(city);

						RPMachine.getInstance().getShopsManager().getShops(city).forEach(AbstractShopSign::breakSign);

						player.sendMessage(ChatColor.RED + "Votre ville a été supprimée.");
					} else {
						player.sendMessage(ChatColor.RED + "ATTENTION ! Vous vous apprêtez à supprimer votre ville !");
						player.sendMessage(ChatColor.RED + "Si vous effectuez cette opération, tous les chunks de votre ville seron réinitialisés");
						player.sendMessage(ChatColor.RED + "Pour confirmer, tapez /city remove confirm");
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
