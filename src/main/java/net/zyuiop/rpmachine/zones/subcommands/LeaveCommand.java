package net.zyuiop.rpmachine.zones.subcommands;

import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.economy.TaxPayerToken;
import net.zyuiop.rpmachine.zones.Zone;
import net.zyuiop.rpmachine.zones.ZonesManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements SubCommand {
	private final ZonesManager manager;

	public LeaveCommand(ZonesManager manager) {
		this.manager = manager;
	}

	@Override
	public String getUsage() {
		return "<parcelle>";
	}

	@Override
	public String getDescription() {
		return "Quitte la zone choisie.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (args.length < 1) {
				player.sendMessage(ChatColor.RED + "Utilisation : /zone leave " + getUsage());
				return;
			}

			Zone plot = manager.getZone(args[0]);
			if (plot == null) {
				player.sendMessage(ChatColor.RED + "Cette zone n'existe pas.");
			} else if (!plot.getOwner().getLandOwner().canManagePlot(player)) {
				player.sendMessage(ChatColor.RED + "Cette zone ne vous appartient pas.");
			} else {
				plot.setOwner((TaxPayerToken) null);
				manager.saveZone(plot);
				player.sendMessage(ChatColor.RED + "Vous n'êtes plus propriétaire de cette zone");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
