package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.common.Area;
import net.zyuiop.rpmachine.common.Selection;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RedefineCommand implements SubCommand {
	private final ProjectsManager manager;

	public RedefineCommand(ProjectsManager manager) {
		this.manager = manager;
	}

	@Override
	public String getUsage() {
		return "<nom> [groundtosky]";
	}

	@Override
	public String getDescription() {
		return "Redéfinit le projet avec la sélection actuelle.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (!player.hasPermission("zones.redefine")) {
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
						player.sendMessage(ChatColor.RED + "Syntaxe invalide : /project redefine " + getUsage());
					} else {
						String name = args[0];
						Project plot = manager.getZone(name);
						if (plot == null) {
							player.sendMessage(ChatColor.RED + "Il n'existe aucune zone de ce nom. Merci d'en créer une.");
							return;
						}

						if (args.length > 1 && args[1].equalsIgnoreCase("groundtosky")) {
							area.setMaxY(254);
							area.setMinY(1);
						}

						// Area check
						if (plot.checkArea(area, manager, player)) {
							manager.saveZone(plot);
							player.sendMessage(ChatColor.GREEN + "Le projet a bien été redéfinie.");
						}
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
