package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class InviteCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public InviteCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "<pseudo>";
	}

	@Override
	public String getDescription() {
		return "Invite un joueur dans votre ville";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'êtes membre d'aucune ville.");
			} else if (city.getMayor().equals(player.getUniqueId()) || city.getCouncils().contains(player.getUniqueId())) {
				if (args.length < 1) {
					player.sendMessage(ChatColor.RED + "Utilisation incorrecte : /city invite " +getUsage());
				} else {
					if (!city.isRequireInvite()) {
						player.sendMessage(ChatColor.RED + "Votre ville ne nécessite pas d'invitation pour être rejointe.");
						return;
					}

					String name = args[0];
					UUID id = RPMachine.database().getUUIDTranslator().getUUID(name, false);
					if (id == null) {
						player.sendMessage(ChatColor.RED + "Ce joueur n'existe pas.");
					} else if (city.getInvitedUsers().contains(id)) {
						player.sendMessage(ChatColor.RED + "Ce joueur est déjà invité dans votre ville.");
					} else if (city.getInhabitants().contains(id)) {
						player.sendMessage(ChatColor.RED + "Ce joueur habite déjà votre ville.");
					} else {
						city.getInvitedUsers().add(id);
						citiesManager.saveCity(city);
						Player target = Bukkit.getPlayer(id);
						if (target != null) {
							target.sendMessage(ChatColor.GOLD + "Vous avez été invité à rejoindre la ville " + ChatColor.YELLOW + city.getCityName() + ChatColor.GOLD + " par " + ChatColor.YELLOW + player.getName());
							target.sendMessage(ChatColor.GOLD + "Pour rejoindre cette ville, utilisez " + ChatColor.YELLOW + "/city join " + city.getCityName());
						}

						player.sendMessage(ChatColor.GREEN + "Le joueur a bien été invité.");
						if (citiesManager.getPlayerCity(id) != null)
							player.sendMessage(ChatColor.GOLD + "Le joueur étant déjà membre d'une ville, il est probable qu'il n'accepte pas votre invitation.");
					}
				}
			} else {
				player.sendMessage(ChatColor.RED + "Seul un membre du conseil municipal peut faire cela..");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
