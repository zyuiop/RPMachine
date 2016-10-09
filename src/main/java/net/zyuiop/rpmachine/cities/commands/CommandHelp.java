package net.zyuiop.rpmachine.cities.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHelp implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender s, Command command, String c, String[] strings) {
		s.sendMessage(ChatColor.GOLD + "Bienvenue sur ce serveur RP !");
		s.sendMessage(ChatColor.GOLD + "Toutes les informations concernant le fonctionnement du jeu se trouvent sur le forum.");
		s.sendMessage(ChatColor.RESET + " ");
		s.sendMessage(ChatColor.GOLD + "" + ChatColor.DARK_AQUA + "Ajouts prévus :");
		s.sendMessage(ChatColor.AQUA + " -> Régions : unions entre villes");
		s.sendMessage(ChatColor.RESET + " ");
		s.sendMessage(ChatColor.GOLD + "" + ChatColor.DARK_GREEN + "Commandes :");
		s.sendMessage(ChatColor.GREEN + "-> /city : commande de gestion de villes");
		s.sendMessage(ChatColor.GREEN + "-> /plot : commande de gestion de parcelles");
		s.sendMessage(ChatColor.GREEN + "-> /floors : liste les paliers de villes disponibles");
		s.sendMessage(ChatColor.GREEN + "-> /createcity : permet de créer une ville");
		s.sendMessage(ChatColor.GREEN + "-> /actas : permet d'agir en tant qu'une autre entité");

		return true;
	}
}
