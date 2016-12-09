package net.zyuiop.rpmachine.economy.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// Unused for now
public class CommandToggleMessages implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (!(commandSender instanceof Player)) { return false; }

		Player player = (Player) commandSender;
		new Thread(() -> {
			PlayerData data = RPMachine.database().getPlayerData(player.getUniqueId());
			boolean val = data.togglePlotMessages();
			commandSender.sendMessage(ChatColor.GOLD + "Messages de parcelles : " + ((val ? ChatColor.GREEN + "Activés" : ChatColor.RED + "Désactivés")));
		}).start();
		return true;
	}
}
