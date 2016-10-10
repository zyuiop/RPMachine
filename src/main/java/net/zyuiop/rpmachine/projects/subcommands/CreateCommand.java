package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.common.Area;
import net.zyuiop.rpmachine.common.Selection;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
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
		return "Crée une parcelle sur votre sélection. Si [groundtosky] est spécifiée, la parcelle sera de la couche 0 à 250.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (!player.hasPermission("zones.create")) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
				return;
			}

			if (RPMachine.getInstance().getSelectionManager().getSelection(player.getUniqueId()) == null) {
				player.sendMessage(ChatColor.RED + "Vous n'avez sélectionné aucune région.");
			} else {
				Selection selection = RPMachine.getInstance().getSelectionManager().getSelection(player.getUniqueId());
				if (selection.getLocation1() == null || selection.getLocation2() == null) {
					player.sendMessage(ChatColor.RED + "Votre sélection n'est pas complète.");
				} else {
					Area area = selection.getArea();
					if (args.length < 1) {
						player.sendMessage(ChatColor.RED + "Syntaxe invalide : /zone create " + getUsage());
					} else {
						String name = args[0];
						if (!name.matches("^[a-zA-Z0-9]{3,16}$")) {
							player.sendMessage(ChatColor.RED + "Le nom de zone doit être composé de 3 à 16 caractères alphanumériques.");
							return;
						}

						if (manager.getZones().containsKey(name)) {
							player.sendMessage(ChatColor.RED + "Ce nom de zone est déjà utilisé.");
							return;
						}

						if (args.length > 1 && args[1].equalsIgnoreCase("groundtosky")) {
							area.setMaxY(254);
							area.setMinY(1);
						}

						// Area check //
						Project plot = new Project();
						plot.setPlotName(name);
						// Area check
						if (plot.checkArea(area, manager, player)) {
							manager.saveZone(plot);
							manager.createZone(plot);
							player.sendMessage(ChatColor.GREEN + "La zone a bien été créée.");
						}
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
