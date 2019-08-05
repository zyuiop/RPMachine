package net.zyuiop.rpmachine.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHelp implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender s, Command command, String c, String[] strings) {
		s.sendMessage(ChatColor.GOLD + "Bienvenue sur Hystoria RP !");
		s.sendMessage(ChatColor.GOLD + "Toutes les informations concernant le fonctionnement du jeu se trouvent sur le document suivant : " + ChatColor.DARK_AQUA + "https://docs.google.com/document/d/1xk8BwfH9Xu5cZuPDc4SZrtHEeGq8oIHw0xfiKzPqBVg/edit#heading=h.cuzj0fv05nc1");
		s.sendMessage(ChatColor.GOLD + "Passez un agréable moment sur Hystoria !");

		return true;
	}
}
