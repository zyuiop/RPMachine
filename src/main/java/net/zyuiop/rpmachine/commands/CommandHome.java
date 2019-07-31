package net.zyuiop.rpmachine.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.economy.commands.EconomixCommand;
import net.zyuiop.rpmachine.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHome extends EconomixCommand {
	private final boolean fromCityOnly;

	public CommandHome(RPMachine economix) {
		super(economix);
		fromCityOnly = economix.getConfig().getBoolean("home.fromCityOnly", false);
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (!(commandSender instanceof Player))
			return false;

		Player player = (Player) commandSender;

		if (fromCityOnly && !RPMachine.getInstance().getCitiesManager().checkCityTeleport(player)) {
			return true;
		}

		new Thread(() -> {
			PlayerData data = RPMachine.database().getPlayerData(player.getUniqueId());
			VirtualLocation loc = data.getHome();
			if (loc == null)
				player.sendMessage(ChatColor.RED + "Vous n'avez pas défini de domicile.");
			else {
				Bukkit.getScheduler().runTask(rpMachine, () -> {
					Location tp = loc.getLocation();
					if (!tp.getChunk().isLoaded())
						tp.getChunk().load();
					player.teleport(tp);
					ReflectionUtils.getVersion().playEndermanTeleport(tp, player);
					player.sendMessage(ChatColor.GOLD + "Vous avez été téléporté !");
				});
			}
		}).start();
		return true;
	}
}
