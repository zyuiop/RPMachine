package net.zyuiop.rpmachine;

import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.economy.AdminLegalEntity;
import net.zyuiop.rpmachine.projects.Project;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author zyuiop
 */
public class CommandActAs implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
		if (strings.length == 0) {
			sender.sendMessage(ChatColor.YELLOW + "Utilisation : /actas <me|city|admin|project <nom du projet>>");
			sender.sendMessage(ChatColor.YELLOW + "Définit la personne en tant que qui vous agissez lorsque vous achetez/vendez.");
			sender.sendMessage(ChatColor.DARK_AQUA + "Exemple : Si vous utilisez /actas city et achetez une parcelle, cette parcelle appartiendra à votre ville.");
			return true;
		}

		String actas = strings[0];
		Player player = (Player) sender;

		/*
		public TaxPayer getLegalEntity() {
		if (isAdmin()) {
			return AdminTaxPayer.INSTANCE;
		} else if (playerUuid != null) {
			return RPMachine.database().getPlayerData(playerUuid);
		} else if (cityName != null) {
			return RPMachine.getInstance().getCitiesManager().getCity(cityName);
		} else if (projectName != null) {
			return RPMachine.getInstance().getProjectsManager().getZone(projectName);
		} else {
			return null;
		}
	}
		 */

		// TODO: make automatic-ish
		switch (actas.toLowerCase()) {
			case "me":
				RPMachine.setPlayerRoleToken(player, RPMachine.database().getPlayerData(player.getUniqueId()));
				sender.sendMessage(ChatColor.GREEN + "Vous agissez désormais en tant que vous même ! (oui ça fait bizarre dit comme ça)");
				break;
			case "city":
				City city = RPMachine.getInstance().getCitiesManager().getPlayerCity(player);

				if (city.getCouncils().contains(player.getUniqueId())) {
					RPMachine.setPlayerRoleToken(player, city);
					sender.sendMessage(ChatColor.GREEN + "Vous agissez désormais au nom de la ville " + ChatColor.DARK_AQUA + city.getCityName());
				} else {
					sender.sendMessage(ChatColor.RED + "Seul le maire peut agir au nom de sa ville !");
				}
				break;
			case "admin":
				if (sender.hasPermission("actas.actAsAdmin")) {
					sender.sendMessage(ChatColor.GREEN + "Vous agissez désormais au nom de §cLa Confédération");
					RPMachine.setPlayerRoleToken((Player) sender, AdminLegalEntity.INSTANCE);
				} else {
					sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'agir au nom de l'Admin");
				}
				break;
			case "project":
				if (strings.length < 2) {
					sender.sendMessage(ChatColor.RED + "Merci d'indiquer un nom de projet.");
					return true;
				}

				Project project = RPMachine.getInstance().getProjectsManager().getZone(strings[1]);
				if (project == null)
					sender.sendMessage(ChatColor.RED + "Projet non trouvé");
				else if (!project.canActAsProject(player))
					sender.sendMessage(ChatColor.RED + "Seul le propriétaire du projet peut agir en tant que projet.");
				else {
					RPMachine.setPlayerRoleToken(player, project);
					sender.sendMessage(ChatColor.GREEN + "Vous agissez désormais au nom de " + project.displayable());
				}

				break;
			default:
				sender.sendMessage(ChatColor.RED + "Entité non reconnue.");
		}

		return true;
	}
}
