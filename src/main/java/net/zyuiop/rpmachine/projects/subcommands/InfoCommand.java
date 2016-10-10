package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.economy.TaxPayerToken;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.joptsimple.internal.Strings;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class InfoCommand implements SubCommand {
	private final ProjectsManager manager;

	public InfoCommand(ProjectsManager manager) {
		this.manager = manager;
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Affiche des informations sur le projet dans lequel vous vous trouvez.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			Project plot = manager.getZoneHere(player.getLocation());
			if (plot == null) {
				player.sendMessage(ChatColor.RED + "Vous ne vous trouvez pas dans un proket.");
			} else {
				player.sendMessage(ChatColor.GOLD + "-----[ Informations Projet ]-----");
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
