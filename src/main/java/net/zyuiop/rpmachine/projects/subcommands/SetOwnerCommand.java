package net.zyuiop.rpmachine.projects.subcommands;

import java.util.UUID;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.Area;
import net.zyuiop.rpmachine.common.Selection;
import net.zyuiop.rpmachine.economy.AdminAccountHolder;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetOwnerCommand implements SubCommand {
	private final ProjectsManager manager;

	public SetOwnerCommand(ProjectsManager manager) {
		this.manager = manager;
	}

	@Override
	public String getUsage() {
		return "<nom> <player <name>|admin|project <name>|city <name>>";
	}

	@Override
	public String getDescription() {
		return "Redéfinit le projet avec la sélection actuelle.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (!player.hasPermission("zones.setowner")) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
				return;
			}

			if (args.length < 2) {
				player.sendMessage(ChatColor.RED + "Arguments manquants : /project " + getUsage());
				return;
			}

			String projectName = args[0];
			String ownerType = args[1];
			String ownerData = (args.length > 2) ? args[2] : null;

			Project project = manager.getZone(projectName);
			if (project == null) {
				player.sendMessage(ChatColor.RED + "Ce projet n'existe pas.");
				return;
			}

			if (ownerType.equalsIgnoreCase("admin")) {
				project.setOwner(AdminAccountHolder.INSTANCE);
			} else if (ownerData == null) {
				player.sendMessage(ChatColor.RED + "Vous devez fournir le nom d'une entité.");
				return;
			} else if (ownerType.equalsIgnoreCase("player")) {
				UUID playerId = RPMachine.database().getUUIDTranslator().getUUID(ownerData);
				if (playerId == null) {
					player.sendMessage(ChatColor.RED + "Ce joueur n'existe pas !");
					return;
				}

				project.setOwner(RPMachine.database().getPlayerData(playerId));
			} else if (ownerType.equalsIgnoreCase("project")) {
				Project parentProject = manager.getZone(projectName);
				if (parentProject == null) {
					player.sendMessage(ChatColor.RED + "Ce projet n'existe pas !");
					return;
				}

				project.setOwner(parentProject);
			} else if (ownerType.equalsIgnoreCase("city")) {
				City city = RPMachine.getInstance().getCitiesManager().getCity(ownerData);
				if (city == null) {
					player.sendMessage(ChatColor.RED + "Cette ville n'existe pas !");
					return;
				}

				project.setOwner(city);
			}

			player.sendMessage(ChatColor.GREEN + "Projet mis à jour.");
			project.save();
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
