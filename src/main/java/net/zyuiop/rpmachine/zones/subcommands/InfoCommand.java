package net.zyuiop.rpmachine.zones.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.economy.TaxPayerToken;
import net.zyuiop.rpmachine.zones.Zone;
import net.zyuiop.rpmachine.zones.ZonesManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class InfoCommand implements SubCommand {
	private final ZonesManager manager;

	public InfoCommand(ZonesManager manager) {
		this.manager = manager;
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Affiche des informations sur la zone dans laquelle vous vous trouvez.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			Zone plot = manager.getZoneHere(player.getLocation());
			if (plot == null) {
				player.sendMessage(ChatColor.RED + "Vous ne vous trouvez pas dans une zone.");
			} else {
				player.sendMessage(ChatColor.GOLD + "-----[ Informations Zone ]-----");
				player.sendMessage(ChatColor.YELLOW + "Nom : " + plot.getPlotName());
				player.sendMessage(ChatColor.YELLOW + "Surface : " + plot.getArea().getSquareArea() + " blocs");
				TaxPayerToken proprio = plot.getOwner();
				if (proprio == null) {
					player.sendMessage(ChatColor.YELLOW + "Propriétaire : " + ChatColor.RED + "Aucun");
				} else {
					String name = proprio.displayable();
					if (name == null) {
						player.sendMessage(ChatColor.YELLOW + "Propriétaire : " + ChatColor.GOLD + "Inconnu");
					} else {
						player.sendMessage(ChatColor.YELLOW + "Propriétaire : " + ChatColor.GREEN + name);
					}
				}

				ArrayList<String> members = new ArrayList<>();
				for (UUID id : plot.getPlotMembers()) {
					String name = RPMachine.database().getUUIDTranslator().getName(id, false);
					if (name != null)
						members.add(name);
				}

				if (members.size() > 0) {
					String mem = Strings.join(members, ", ");
					player.sendMessage(ChatColor.YELLOW + "Membres : " + mem);
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
