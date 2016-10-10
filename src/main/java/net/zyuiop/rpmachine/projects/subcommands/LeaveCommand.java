package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.economy.AdminAccountHolder;
import net.zyuiop.rpmachine.economy.TaxPayerToken;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements SubCommand {
	private final ProjectsManager manager;

	public LeaveCommand(ProjectsManager manager) {
		this.manager = manager;
	}

	@Override
	public String getUsage() {
		return "<parcelle>";
	}

	@Override
	public String getDescription() {
		return "Quitte le projet choisi.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length < 1) {
				player.sendMessage(ChatColor.RED + "Utilisation : /project leave " + getUsage());
				return;
			}

			Project plot = manager.getZone(args[0]);
			if (plot == null) {
				player.sendMessage(ChatColor.RED + "Ce projet n'existe pas.");
			} else if (!plot.getOwner().getLandOwner().canManagePlot(player)) {
				player.sendMessage(ChatColor.RED + "Ce projet ne vous appartient pas.");
			} else {
				plot.setOwner(AdminAccountHolder.INSTANCE);
				manager.saveZone(plot);
				player.sendMessage(ChatColor.RED + "Vous n'êtes plus propriétaire de ce projet");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
