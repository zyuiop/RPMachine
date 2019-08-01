package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.shops.types.AbstractShopSign;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class RemoveCommand implements CityMemberSubCommand {
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
		return "supprime et réinitialise les chunks de votre ville";
	}

	@Override
	public boolean requiresMayorPrivilege() {
		return true;
	}

	@Override
	public boolean run(Player player, @Nonnull City city, String[] args) {
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

		return true;
	}
}
