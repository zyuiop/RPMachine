package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.common.regions.RectangleRegion;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.common.regions.Region;
import net.zyuiop.rpmachine.common.selections.PlayerSelection;
import net.zyuiop.rpmachine.common.selections.RectangleSelection;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.common.selections.Selection;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.stream.StreamSupport;

public class RedefineCommand implements CityMemberSubCommand {
	private final CitiesManager citiesManager;

	public RedefineCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "<nom>";
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
		if (PlayerSelection.getPlayerSelection(player) == null) {
			player.sendMessage(ChatColor.RED + "Vous n'avez sélectionné aucune région.");
			return false;
		} else {
			Selection selection = PlayerSelection.getPlayerSelection(player);

			try {
				Region area = selection.getRegion();

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
						area.expandY(-255);
						area.expandY(255);

						player.sendMessage(ChatColor.RED + "L'option groundtosky n'est plus supportée, utilisez plutôt " + ChatColor.YELLOW + "/sel expand");
					}

					boolean mustStop = StreamSupport.stream(area.spliterator(), false)
							.anyMatch(block -> {
								if (!city.getChunks().contains(new VirtualChunk(block.getChunk()))) {
									player.sendMessage(ChatColor.RED + "Une partie de votre sélection est hors de la ville.");
									return true;
								}

								Plot check = city.getPlotHere(block.getLocation());
								if (check != null && !check.getPlotName().equals(plot.getPlotName())) {
									player.sendMessage(ChatColor.RED + "Une partie de votre sélection fait partie d'une autre parcelle.");
									return true;
								}

								return false;
							});

					if (mustStop)
						return true;

					plot.setArea(area);
					city.save();


					player.sendMessage(ChatColor.GREEN + "La parcelle a bien été redéfinie.");
				}
			} catch (Exception e) {
				player.sendMessage(ChatColor.RED + "Sélection invalide : " + e.getMessage());
			}
		}

		return true;
	}
}
