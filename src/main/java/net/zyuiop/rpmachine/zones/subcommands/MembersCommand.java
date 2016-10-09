package net.zyuiop.rpmachine.zones.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.zones.Zone;
import net.zyuiop.rpmachine.zones.ZonesManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MembersCommand implements SubCommand {
	private final ZonesManager manager;

	public MembersCommand(ZonesManager manager) {
		this.manager = manager;
	}

	@Override
	public String getUsage() {
		return "<zone> <add|remove> <joueur>";
	}

	@Override
	public String getDescription() {
		return "Ajoute ou supprime un membre de votre zone.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length < 4) {
				player.sendMessage(ChatColor.RED + "Utilisation : /zone members " + getUsage());
				return;
			}

			Zone plot = manager.getZone(args[0]);
			if (plot == null) {
				player.sendMessage(ChatColor.RED + "Cette zone n'existe pas.");
			} else if (plot.getOwner().getLandOwner().canManagePlot(player) && !player.hasPermission("zone.members.manage")) {
				player.sendMessage(ChatColor.RED + "Cette parcelle ne vous appartient pas.");
			} else {
				UUID id = RPMachine.database().getUUIDTranslator().getUUID(args[2], true);
				if (id == null) {
					player.sendMessage(ChatColor.RED + "Ce joueur n'a pas été trouvé.");
				} else {
					if (args[1].equalsIgnoreCase("add")) {
						if (plot.getPlotMembers().contains(id)) {
							player.sendMessage(ChatColor.GREEN + "Ce joueur est déjà dans la zone.");
							return;
						}
						plot.getPlotMembers().add(id);
						manager.saveZone(plot);
						player.sendMessage(ChatColor.GREEN + "Le joueur a été ajouté dans la zone.");
					} else if (args[1].equalsIgnoreCase("remove")) {
						if (!plot.getPlotMembers().contains(id)) {
							player.sendMessage(ChatColor.GREEN + "Ce joueur n'est pas dans la zone.");
							return;
						}
						plot.getPlotMembers().remove(id);
						manager.saveZone(plot);
						player.sendMessage(ChatColor.GREEN + "Le joueur a été supprimé de la zone.");
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
