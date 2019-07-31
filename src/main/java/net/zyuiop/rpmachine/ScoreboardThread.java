package net.zyuiop.rpmachine;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.reflection.ReflectionUtils;
import net.zyuiop.rpmachine.utils.DirectionArrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ScoreboardThread implements Runnable {

	private final Player player;
	private final UUID uuid;
	private final ScoreboardSign sign;
	private int elapsed = 0;

	public ScoreboardThread(Player player) {
		this.player = player;
		this.uuid = player.getUniqueId();
		sign = ReflectionUtils.getVersion().createScoreboardSign(player, ChatColor.GOLD + "" + ChatColor.BOLD + "Infos - " + player.getName());
		sign.create();
	}


	public void personnalBoard() {
		sign.setObjectiveName(ChatColor.GOLD + "" + ChatColor.BOLD + "Infos - " + player.getName());

		PlayerData data = RPMachine.getInstance().getDatabaseManager().getPlayerData(uuid);
		double money = data.getBalance();
		String job = data.getJob();
		job = job != null ? ChatColor.AQUA + job : ChatColor.RED + "Aucun";
		VirtualLocation homeData = data.getHome();
		String home;
		if (homeData == null)
			home = ChatColor.RED + "Non défini";
		else if (!player.getWorld().getName().equalsIgnoreCase("world"))
			home = ChatColor.RED + "Autre monde";
		else {
			try {
				Location location = homeData.getLocation();
				double dist = location.distance(player.getLocation());
				String arrow = DirectionArrows.getArrow(player.getLocation(), location);
				home = ChatColor.GREEN + arrow + " " + (Math.round(dist * 10)/10.0) + "m";
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
		sign.setLine(3, ChatColor.AQUA + "" + money + " " + EconomyManager.getMoneyName());
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

		sign.setObjectiveName(ChatColor.DARK_AQUA + "" + ChatColor.BOLD +  "Ville : " + city.getCityName());

		sign.setLine(1, ChatColor.RED + "  ");
		sign.setLine(2, ChatColor.YELLOW + "" + ChatColor.BOLD + "-> Monnaie");
		sign.setLine(3, ChatColor.AQUA + "" + city.getBalance() + " " + EconomyManager.getMoneyName());
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

		if (elapsed > 100 && RPMachine.getInstance().getCitiesManager().getPlayerCity(player.getUniqueId()) != null) {
			cityBoard();
		} else if (elapsed > 100) {
			elapsed = 0;
			personnalBoard();
		} else {
			personnalBoard();
		}

		elapsed++;

		if (elapsed > 200)
			elapsed = 0;

		LegalEntity le = RPMachine.getPlayerActAs(player);
		if (!(le instanceof PlayerData)) {
			String actAs = "§cVous agissez en tant que : " + le.displayable();

			player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(actAs));
		}
		// sign.updateLines();
	}
}
