package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.common.Area;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.common.Selection;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class RedefineCommand implements CityMemberSubCommand {
	private final CitiesManager citiesManager;

	public RedefineCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "<nom> [groundtosky]";
	}

	@Override
	public String getDescription() {
		return "redéfinit la parcelle avec la sélection actuelle";
	}

	@Override
	public boolean requiresCouncilPrivilege() {
		return true;
	}

	@Override
	public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
		if (RPMachine.getInstance().getSelectionManager().getSelection(player.getUniqueId()) == null) {
			player.sendMessage(ChatColor.RED + "Vous n'avez sélectionné aucune région.");
			return false;
		} else {
			Selection selection = RPMachine.getInstance().getSelectionManager().getSelection(player.getUniqueId());
			if (selection.getLocation1() == null || selection.getLocation2() == null) {
				player.sendMessage(ChatColor.RED + "Votre sélection n'est pas complète.");
			} else {
				Area area = selection.getArea();
				if (args.length < 1) {
					player.sendMessage(ChatColor.RED + "Syntaxe invalide : /plot redefine " + getUsage());
				} else {
					String name = args[0];
					Plot plot = city.getPlot(name);
					if (plot == null) {
						player.sendMessage(ChatColor.RED + "Il n'existe aucune parcelle de ce nom. Merci d'en créer une.");
						return true;
					}

					if (plot.getOwner() != null && !city.hasPermission(player, CityPermissions.REDEFINE_OCCUPIED_PLOT)) {
						player.sendMessage(ChatColor.RED + "Vous ne pouvez pas redéfinir de plot occupé.");
						return true;
					} else if (plot.getOwner() == null && !city.hasPermission(player, CityPermissions.REDEFINE_EMPTY_PLOT)) {
						player.sendMessage(ChatColor.RED + "Vous ne pouvez pas redéfinir de plot vide.");
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
								Plot check = city.getPlotHere(new Location(Bukkit.getWorld("world"), i_x, i_y, i_z));
								if (check != null && !check.getPlotName().equals(plot.getPlotName())) {
									player.sendMessage(ChatColor.RED + "Une partie de votre sélection fait partie d'une autre parcelle.");
									return true;
								}
								i_y++;
							}
							i_z++;
						}
						i_x++;
					}

					plot.setArea(area);
					city.save();


					player.sendMessage(ChatColor.GREEN + "La parcelle a bien été redéfinie.");
				}
			}
		}

		return true;
	}
}
