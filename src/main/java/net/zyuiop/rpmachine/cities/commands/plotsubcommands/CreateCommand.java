package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.common.regions.RectangleRegion;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.common.selections.RectangleSelection;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class CreateCommand implements CityMemberSubCommand {
	private final CitiesManager citiesManager;

	public CreateCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "<nom> [groundtosky]";
	}

	@Override
	public String getDescription() {
		return "crée une parcelle sur la sélection, ou jusqu'au ciel si [groundtosky] est spécifiée";
	}

	@Override
	public Permission requiresPermission() {
		return CityPermissions.CREATE_PLOT;
	}

	@Override
	public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
		if (RPMachine.getInstance().getSelectionManager().getSelection(player.getUniqueId()) == null) {
			player.sendMessage(ChatColor.RED + "Vous n'avez sélectionné aucune région.");
			return false;
		} else {
			RectangleSelection selection = RPMachine.getInstance().getSelectionManager().getSelection(player.getUniqueId());
			if (selection.getLocation1() == null || selection.getLocation2() == null) {
				player.sendMessage(ChatColor.RED + "Votre sélection n'est pas complète.");
			} else {
				RectangleRegion area = selection.getArea();
				if (args.length < 1) {
					player.sendMessage(ChatColor.RED + "Syntaxe invalide : /plot create " + getUsage());
				} else {
					String name = args[0];
					if (!name.matches("^[a-zA-Z0-9]{3,16}$")) {
						player.sendMessage(ChatColor.RED + "Le nom de parcelles doit être composé de 3 à 16 caractères alphanumériques.");
						return false;
					}

					if (city.getPlot(name) != null) {
						player.sendMessage(ChatColor.RED + "Ce nom de parcelle est déjà utilisé.");
						return true;
					}

					if (args.length > 1 && args[1].equalsIgnoreCase("groundtosky")) {
						area.setMaxY(254);
						area.setMinY(1);
					}

					// Area check //
					int i_x = area.getMinX();
					while (i_x < area.getMaxX()) {
						int i_z = area.getMinZ();
						while (i_z < area.getMaxZ()) {
							if (!city.getChunks().contains(new VirtualChunk(new Location(Bukkit.getWorld("world"), i_x, 64, i_z).getChunk()))) {
								player.sendMessage(ChatColor.RED + "Une partie de votre sélection est hors de la ville.");
								return true;
							}

							int i_y = area.getMinY();
							while (i_y < area.getMaxY()) {
								if (city.getPlotHere(new Location(Bukkit.getWorld("world"), i_x, i_y, i_z)) != null) {
									player.sendMessage(ChatColor.RED + "Une partie de votre sélection fait partie d'une autre parcelle.");
									return true;
								}
								i_y ++;
							}
							i_z ++;
						}
						i_x ++;
					}

					Plot plot = new Plot();
					plot.setPlotName(name);
					plot.setArea(area);

					city.addPlot(name, plot);
					player.sendMessage(ChatColor.GREEN + "La parcelle a bien été créée.");
				}
			}
		}
		return true;
	}
}
