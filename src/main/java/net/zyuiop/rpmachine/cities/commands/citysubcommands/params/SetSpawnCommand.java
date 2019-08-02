package net.zyuiop.rpmachine.cities.commands.citysubcommands.params;

import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class SetSpawnCommand implements CityMemberSubCommand {

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
		return "définit le spawn de la ville";
	}

	@Override
	public Permission requiresPermission() {
		return CityPermissions.SET_SPAWN;
	}

	@Override
	public boolean run(Player player, @Nonnull City city, String[] args) {
		Location loc = player.getLocation();
		if (!loc.getWorld().getName().equals("world") || !city.getChunks().contains(new VirtualChunk(loc.getChunk()))) {
			player.sendMessage(ChatColor.RED + "Le point de spawn doit se trouver dans votre ville.");
		} else {
			city.setSpawn(new VirtualLocation(loc));
			citiesManager.saveCity(city);
			player.sendMessage(ChatColor.GREEN + "Le spawn de votre ville a été défini !");
		}

		return true;
	}
}
