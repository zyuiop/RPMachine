package net.zyuiop.rpmachine.economy.commands;

import net.bridgesapi.api.BukkitBridge;
import net.bridgesapi.api.player.PlayerData;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHome extends EconomixCommand {
	public CommandHome(RPMachine economix) {
		super(economix);
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (!(commandSender instanceof Player))
			return false;

		Player player = (Player) commandSender;
		new Thread(() -> {
			PlayerData data = BukkitBridge.get().getPlayerManager().getPlayerData(player.getUniqueId());
			String loc = data.get("rp.home");
			if (loc == null)
				player.sendMessage(ChatColor.RED + "Vous n'avez pas défini de domicile.");
			else {
				Bukkit.getScheduler().runTask(rpMachine, () -> {
					Location tp = new VirtualLocation(loc).getLocation();
					player.teleport(tp);
					player.playSound(tp, Sound.ENDERMAN_TELEPORT, 1, 1);
					player.sendMessage(ChatColor.GOLD + "Vous avez été téléporté !");
				});
			}
		}).start();
		return true;
	}
}
