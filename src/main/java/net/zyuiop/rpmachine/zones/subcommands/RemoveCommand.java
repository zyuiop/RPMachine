package net.zyuiop.rpmachine.zones.subcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.zones.Zone;
import net.zyuiop.rpmachine.zones.ZonesManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveCommand implements SubCommand {
	private final ZonesManager manager;

	public RemoveCommand(ZonesManager manager) {
		this.manager = manager;
	}

	@Override
	public String getUsage() {
		return "<nom>";
	}

	@Override
	public String getDescription() {
		return "Supprime la zone <nom>";
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
				Zone plot = manager.getZone(args[0]);
				if (plot == null) {
					player.sendMessage(ChatColor.RED + "Il n'existe aucune zone de ce nom. Merci d'en créer une.");
					return;
				}

				manager.removeZone(plot);
				player.sendMessage(ChatColor.GREEN + "La zone a bien été supprimée.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
