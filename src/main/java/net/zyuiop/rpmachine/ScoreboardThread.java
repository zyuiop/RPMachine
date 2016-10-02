package net.zyuiop.rpmachine;

import net.bridgesapi.api.BukkitBridge;
import net.bridgesapi.api.player.PlayerData;
import net.bridgesapi.tools.scoreboards.ObjectiveSign;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class ScoreboardThread implements Runnable {

	private final Player player;
	private final UUID uuid;
	private final ObjectiveSign sign;
	private int elapsed = 0;

	public ScoreboardThread(Player player) {
		this.player = player;
		this.uuid = player.getUniqueId();
		sign = new ObjectiveSign(player.getName(), ChatColor.GOLD + "" + ChatColor.BOLD + "Infos - " + player.getName());
		sign.addReceiver(player);
	}


	public void personnalBoard() {
		sign.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Infos - " + player.getName());

		PlayerData data = BukkitBridge.get().getPlayerManager().getPlayerData(uuid);
		double money = Math.round(data.getDouble("rpmoney", 0.0)*100) / 100;
		String job = ChatColor.AQUA + data.get("job", ChatColor.RED + "Aucun");
		String homeData = data.get("rp.home", null);
		String home;
		if (homeData == null)
			home = ChatColor.RED + "Non défini";
		else if (!player.getWorld().getName().equalsIgnoreCase("world"))
			home = ChatColor.RED + "Autre monde";
		else {
			try {
				VirtualLocation loc = new VirtualLocation(homeData);
				Location location = loc.getLocation();
				double dist = location.distance(player.getLocation());
				home = ChatColor.GREEN + "" + (Math.round(dist * 10)/10.0) + "m";
			} catch (Exception e) {
				home = ChatColor.RED + "Erreur";
			}
		}

		Location spawnLoc = Bukkit.getWorld("world").getSpawnLocation();
		String spawn;
		if (!player.getWorld().getName().equalsIgnoreCase("world"))
			spawn = ChatColor.RED + "Autre monde";
		else {
			double dist = spawnLoc.distance(player.getLocation());
			spawn = ChatColor.GREEN + "" + (Math.round(dist * 10)/10.0) + "m";
		}

		long time = player.getWorld().getTime();
		double hours = Math.floor(time / 1000);
		double minutes = Math.floor((time - (hours * 1000)) / 10);
		hours += 6;
		if (hours >= 24)
			hours -= 24;

		if (minutes > 0) {
			minutes = (minutes / 10) * 6;
			minutes = Math.floor(minutes);
		}

		String c = ChatColor.DARK_GREEN + "Nature";
		if (player.getLocation() != null) {
			City city = RPMachine.getInstance().getCitiesManager().getCityHere(player.getLocation().getChunk());
			c = (city == null) ? ChatColor.DARK_GREEN + "Nature" : ChatColor.AQUA + city.getCityName();
		}

		sign.setLine(1, ChatColor.RED + "  ");
		sign.setLine(2, ChatColor.YELLOW + "" + ChatColor.BOLD + "-> Monnaie");
		sign.setLine(3, ChatColor.AQUA + "" + money + " $");
		sign.setLine(4, ChatColor.RED + "   ");
		sign.setLine(5, ChatColor.YELLOW + "" + ChatColor.BOLD +  "-> Métier");
		sign.setLine(6, job);
		sign.setLine(7, ChatColor.RED + "    ");
		sign.setLine(8, ChatColor.YELLOW + "" + ChatColor.BOLD +  "-> Maison");
		sign.setLine(9, home);
		sign.setLine(10, ChatColor.RED + "     ");
		sign.setLine(11, ChatColor.YELLOW + "" + ChatColor.BOLD +  "-> Ville actuelle");
		sign.setLine(12, c);
		sign.setLine(13, ChatColor.RED + "      ");
		sign.setLine(14, ChatColor.YELLOW + "" + ChatColor.BOLD +  "-> Monde & heure");
		sign.setLine(15, ChatColor.AQUA + player.getWorld().getName() + ChatColor.GREEN + ", " + ((hours < 10) ? "0" : "") + ((int) hours) + " h " + ((minutes < 10) ? "0" : "") + ((int) minutes));

	}


	public void cityBoard() {
		City city = RPMachine.getInstance().getCitiesManager().getPlayerCity(player.getUniqueId());
		if (city == null) {
			elapsed = 0;
			return;
		}

		sign.setDisplayName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD +  "Ville : " + city.getCityName());

		sign.setLine(1, ChatColor.RED + "  ");
		sign.setLine(2, ChatColor.YELLOW + "" + ChatColor.BOLD + "-> Monnaie");
		sign.setLine(3, ChatColor.AQUA + "" + city.getMoney() + " $");
		sign.setLine(4, ChatColor.RED + "   ");
		sign.setLine(5, ChatColor.YELLOW + "" + ChatColor.BOLD +  "-> Habitants");
		sign.setLine(6, ChatColor.AQUA + "" + city.countInhabitants() + " Hab.");
		sign.setLine(7, ChatColor.RED + "    ");
		sign.setLine(8, ChatColor.YELLOW + "" + ChatColor.BOLD +  "-> Surface");
		sign.setLine(9, ChatColor.AQUA + "" + city.getChunks().size() + " chunks");
		sign.setLine(10, ChatColor.RED + "     ");
		sign.setLine(11, ChatColor.YELLOW + "" + ChatColor.BOLD +  "-> Palier");
		sign.setLine(12, ChatColor.AQUA + "" + RPMachine.getInstance().getCitiesManager().getFloor(city).getName());
		sign.setLine(13, ChatColor.RED + "      ");
		sign.setLine(14, ChatColor.YELLOW + "" + ChatColor.BOLD +  "-> Type");
		sign.setLine(15, ((city.isRequireInvite() ? ChatColor.RED + "Sur invitation" : ChatColor.GREEN + "Publique")));

	}

	@Override
	public void run() {
		if (!player.isOnline()) {
			RPMachine.getInstance().getScoreboardManager().removePlayer(uuid);
			return;
		}

		if (elapsed > 25 && RPMachine.getInstance().getCitiesManager().getPlayerCity(player.getUniqueId()) != null) {
			cityBoard();
		} else if (elapsed > 25) {
			elapsed = 0;
			personnalBoard();
		} else {
			personnalBoard();
		}

		elapsed++;

		if (elapsed > 50)
			elapsed = 0;


		sign.updateLines();
	}
}
