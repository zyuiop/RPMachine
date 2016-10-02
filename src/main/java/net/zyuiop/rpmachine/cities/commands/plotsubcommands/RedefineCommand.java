package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RedefineCommand implements SubCommand {
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
		return "Redéfinit la parcelle avec la sélection actuelle.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas de ville.");
			} else if (!city.getCouncils().contains(player.getUniqueId()) && !player.getUniqueId().equals(city.getMayor())) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de définir des parcelles dans votre ville.");
			} else if (RPMachine.getInstance().getSelectionManager().getSelection(player.getUniqueId()) == null) {
				player.sendMessage(ChatColor.RED + "Vous n'avez sélectionné aucune région.");
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
						Plot plot = city.getPlots().get(name);
						if (plot == null) {
							player.sendMessage(ChatColor.RED + "Il n'existe aucune parcelle de ce nom. Merci d'en créer une.");
							return;
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
									return;
								}

								int i_y = area.getMinY();
								while (i_y < area.getMaxY()) {
									Plot check = city.getPlotHere(new Location(Bukkit.getWorld("world"), i_x, i_y, i_z));
									if (check != null && !check.getPlotName().equals(plot.getPlotName())) {
										player.sendMessage(ChatColor.RED + "Une partie de votre sélection fait partie d'une autre parcelle.");
										return;
									}
									i_y ++;
								}
								i_z ++;
							}
							i_x ++;
						}

						plot.setArea(area);

						city.getPlots().put(name, plot);
						citiesManager.saveCity(city);

						player.sendMessage(ChatColor.GREEN + "La parcelle a bien été redéfinie.");
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
