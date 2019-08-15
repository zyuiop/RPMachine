package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.common.regions.RectangleRegion;
import net.zyuiop.rpmachine.common.regions.Region;
import net.zyuiop.rpmachine.common.selections.PlayerSelection;
import net.zyuiop.rpmachine.common.selections.RectangleSelection;
import net.zyuiop.rpmachine.common.selections.Selection;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CreateCommand implements SubCommand {
	private final ProjectsManager manager;

	public CreateCommand(ProjectsManager manager) {
		this.manager = manager;
	}

	@Override
	public String getUsage() {
		return "<nom> [groundtosky]";
	}

	@Override
	public String getDescription() {
		return "crée un projet sur la sélection, ou jusqu'au ciel si [groundtosky] est spécifiée";
	}

	@Override
	public boolean run(Player player, String command, String subCommand, String[] args) {
		if (PlayerSelection.getPlayerSelection(player) == null) {
			player.sendMessage(ChatColor.RED + "Vous n'avez sélectionné aucune région.");
			return false;
		} else {
			Selection selection = PlayerSelection.getPlayerSelection(player);

			try {
				Region area = selection.getRegion();
				if (args.length < 1) {
					player.sendMessage(ChatColor.RED + "Syntaxe invalide : /zone create " + getUsage());
				} else {
					String name = args[0];
					if (!name.matches("^[a-zA-Z0-9]{3,16}$")) {
						player.sendMessage(ChatColor.RED + "Le nom de zone doit être composé de 3 à 16 caractères alphanumériques.");
						return false;
					}

					if (manager.getZones().containsKey(name)) {
						player.sendMessage(ChatColor.RED + "Ce nom de zone est déjà utilisé.");
						return true;
					}

					if (args.length > 1 && args[1].equalsIgnoreCase("groundtosky")) {
						area.expandY(-255);
						area.expandY(255);

						player.sendMessage(ChatColor.RED + "L'option groundtosky n'est plus supportée, utilisez plutôt " + ChatColor.YELLOW + "/sel expand");
					}

					// Area check //
					Project plot = new Project();
					plot.setPlotName(name);
					// Area check
					if (plot.checkArea(area, manager, player)) {
						manager.createZone(plot);
						player.sendMessage(ChatColor.GREEN + "La zone a bien été créée.");
					}
				}
			} catch (Exception e) {
				player.sendMessage(ChatColor.RED + "Sélection invalide : " + e.getMessage());
			}
		}
		return true;
	}

	@Override
	public boolean canUse(Player player) {
		return player.hasPermission("zones.create");
	}
}
