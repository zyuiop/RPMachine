package net.zyuiop.rpmachine.commands;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandInventory implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (!commandSender.hasPermission("rp.inventory")) {
			commandSender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission !");
			return true;
		}

		if (strings.length < 1) {
			commandSender.sendMessage(ChatColor.RED + "Erreur : pseudo manquant.");
			return true;
		}

		Player target = Bukkit.getPlayerExact(strings[0]);
		if (target == null) {
			commandSender.sendMessage(ChatColor.RED + "Joueur introuvable.");
			return true;
		}
		Player player = (Player) commandSender;
		player.openInventory(target.getInventory());
		return true;
	}
}
