package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.economy.shops.AbstractShopSign;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveCommand implements SubCommand {
	private final ProjectsManager manager;

	public RemoveCommand(ProjectsManager manager) {
		this.manager = manager;
	}

	@Override
	public String getUsage() {
		return "<nom>";
	}

	@Override
	public String getDescription() {
		return "Supprime le projet <nom>";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (!player.hasPermission("zones.remove")) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
				return;
			}

			if (args.length < 1) {
				player.sendMessage(ChatColor.RED + "Argument manquant.");
			} else {
				Project plot = manager.getZone(args[0]);
				if (plot == null) {
					player.sendMessage(ChatColor.RED + "Il n'existe aucun projet de ce nom. Merci d'en créer une.");
					return;
				}

				RPMachine.getInstance().getShopsManager().getShops(plot).forEach(AbstractShopSign::breakSign);

				manager.removeZone(plot);
				player.sendMessage(ChatColor.GREEN + "Le projet a bien été supprimée.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
