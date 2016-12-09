package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MembersCommand implements SubCommand {
	private final ProjectsManager manager;

	public MembersCommand(ProjectsManager manager) {
		this.manager = manager;
	}

	@Override
	public String getUsage() {
		return "<projet> <add|remove> <joueur>";
	}

	@Override
	public String getDescription() {
		return "Ajoute ou supprime un membre de votre projet.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length < 4) {
				player.sendMessage(ChatColor.RED + "Utilisation : /project members " + getUsage());
				return;
			}

			Project plot = manager.getZone(args[0]);
			if (plot == null) {
				player.sendMessage(ChatColor.RED + "Ce projet n'existe pas.");
			} else if (plot.getOwner().getLandOwner().canManagePlot(player) && !player.hasPermission("zone.members.manage")) {
				player.sendMessage(ChatColor.RED + "Ce projet ne vous appartient pas.");
			} else {
				UUID id = RPMachine.database().getUUIDTranslator().getUUID(args[2], true);
				if (id == null) {
					player.sendMessage(ChatColor.RED + "Ce joueur n'a pas été trouvé.");
				} else {
					if (args[1].equalsIgnoreCase("add")) {
						if (plot.getPlotMembers().contains(id)) {
							player.sendMessage(ChatColor.GREEN + "Ce joueur est déjà dans le projet.");
							return;
						}
						plot.getPlotMembers().add(id);
						manager.saveZone(plot);
						player.sendMessage(ChatColor.GREEN + "Le joueur a été ajouté dans le projet.");
					} else if (args[1].equalsIgnoreCase("remove")) {
						if (!plot.getPlotMembers().contains(id)) {
							player.sendMessage(ChatColor.GREEN + "Ce joueur n'est pas dans le projet.");
							return;
						}
						plot.getPlotMembers().remove(id);
						manager.saveZone(plot);
						player.sendMessage(ChatColor.GREEN + "Le joueur a été supprimé du projet.");
					} else {
						player.sendMessage(ChatColor.RED + "Argument invalide (add / remove)");
					}
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
