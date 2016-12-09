package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.CityFloor;
import net.zyuiop.rpmachine.economy.EconomyManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class InfoCommand implements SubCommand {

	private final CitiesManager citiesManager;
	public InfoCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}


	@Override
	public String getUsage() {
		return "[ville]";
	}

	@Override
	public String getDescription() {
		return "Affiche les informations de la ville où vous vous trouvez ou de [ville] passée en argument.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		City target = null;
		if (args.length > 0) {
			target = citiesManager.getCity(args[0]);
		} else if (sender instanceof Player) {
			if (!((Player) sender).getLocation().getWorld().getName().equalsIgnoreCase("world")) {
				sender.sendMessage(ChatColor.RED + "Vous n'êtes dans aucune ville actuellement.");
				return;
			}

			target = citiesManager.getCityHere(((Player) sender).getLocation().getChunk());
			if (target == null)
				sender.sendMessage(ChatColor.RED + "Vous n'êtes dans aucune ville actuellement.");
		}

		if (target != null) {
			sender.sendMessage(ChatColor.YELLOW + "-----[ Ville de " + ChatColor.GOLD + target.getCityName() + ChatColor.YELLOW + " ]-----");
			sender.sendMessage(ChatColor.YELLOW + "Maire actuel : " + RPMachine.database().getUUIDTranslator().getName(target.getMayor()));
			sender.sendMessage(ChatColor.YELLOW + "Nombre d'habitants : " + target.countInhabitants());
			sender.sendMessage(ChatColor.YELLOW + "Type de ville : " + ((target.isRequireInvite() ? ChatColor.RED + "Sur invitation" : ChatColor.GREEN + "Publique")));
			sender.sendMessage(ChatColor.YELLOW + "Impôts : " + target.getTaxes() + " " + EconomyManager.getMoneyName() + " par semaine");

			CityFloor floor = citiesManager.getFloor(target);
			sender.sendMessage(ChatColor.YELLOW + "Palier : " + floor.getName());

			if (sender instanceof ConsoleCommandSender || (sender instanceof Player && (((Player) sender).getUniqueId().equals(target.getMayor()) ||target.getCouncils().contains(((Player) sender).getUniqueId())))) {
				sender.sendMessage(ChatColor.YELLOW + "Monnaie : " +  target.getMoney() + " " + EconomyManager.getMoneyName());
				sender.sendMessage(ChatColor.YELLOW + "Taille : " + target.getChunks().size() + " / " + floor.getMaxsurface());
			}
		}

	}
}
