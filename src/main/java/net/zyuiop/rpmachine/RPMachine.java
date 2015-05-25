package net.zyuiop.rpmachine;

import net.bridgesapi.api.BukkitBridge;
import net.bridgesapi.api.player.PlayerData;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.SelectionManager;
import net.zyuiop.rpmachine.cities.commands.*;
import net.zyuiop.rpmachine.cities.listeners.CitiesListener;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.economy.ShopsManager;
import net.zyuiop.rpmachine.economy.TransactionsHelper;
import net.zyuiop.rpmachine.economy.commands.*;
import net.zyuiop.rpmachine.economy.jobs.JobsManager;
import net.zyuiop.rpmachine.economy.listeners.PlayerListener;
import net.zyuiop.rpmachine.economy.listeners.SignsListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class RPMachine extends JavaPlugin {

	private static RPMachine instance;
	private TransactionsHelper transactionsHelper;
	private EconomyManager economyManager;
	private ShopsManager shopsManager;
	private JobsManager jobsManager;
	private CitiesManager citiesManager;
	private SelectionManager selectionManager;
	private ScoreboardManager scoreboardManager;

	public static RPMachine getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;

		this.economyManager = new EconomyManager();
		this.shopsManager = new ShopsManager();
		this.transactionsHelper = new TransactionsHelper(this.economyManager);
		this.jobsManager = new JobsManager(this);
		this.citiesManager = new CitiesManager(this);
		this.selectionManager = new SelectionManager(citiesManager);
		this.scoreboardManager = new ScoreboardManager(this);

		getCommand("city").setExecutor(new CityCommand(citiesManager));
		getCommand("plot").setExecutor(new PlotCommand(citiesManager));
		getCommand("createcity").setExecutor(new CreateCityCommand(citiesManager));
		getCommand("floors").setExecutor(new FloorsCommand(citiesManager));

		getCommand("pay").setExecutor(new CommandPay(this));
		getCommand("money").setExecutor(new CommandMoney(this));
		getCommand("fly").setExecutor(new CommandFly());
		getCommand("home").setExecutor(new CommandHome(this));
		getCommand("sethome").setExecutor(new CommandSethome());
		getCommand("endsee").setExecutor(new CommandEnderchest());
		getCommand("jobs").setExecutor(new CommandJob(this));
		getCommand("spawn").setExecutor(new CommandSpawn());
		getCommand("info").setExecutor(new CommandHelp());
		getCommand("runtaxes").setExecutor(new CommandRuntaxes(citiesManager));
		getCommand("bypass").setExecutor(new CommandBypass(citiesManager));

		Bukkit.getPluginManager().registerEvents(selectionManager, this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
		Bukkit.getPluginManager().registerEvents(new SignsListener(this), this);
		Bukkit.getPluginManager().registerEvents(new CitiesListener(citiesManager), this);



		Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				PlayerData data = BukkitBridge.get().getPlayerManager().getPlayerData(player.getUniqueId());
				if (data.get("job") == null)
					player.sendMessage(ChatColor.GOLD + "Vous n'avez pas encore choisi de métier. Tapez /job pour plus d'infos.");
			}
		}, 100L, 3 * 60 * 20L);

		try {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(new Date());
			if (calendar.get(Calendar.HOUR_OF_DAY) >= 4)
				calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
			calendar.set(Calendar.HOUR_OF_DAY, 4);
			calendar.set(Calendar.MINUTE, 0);
			Date sched = calendar.getTime();

			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					Bukkit.getServer().savePlayers();
					Bukkit.getWorld("world").save();
					Bukkit.getServer().shutdown();
				}
			}, sched);
			this.getLogger().info("Scheduled automatic reboot at : " + calendar.toString());
		} catch (Exception e) {
			this.getLogger().severe("CANNOT SCHEDULE AUTOMATIC SHUTDOWN.");
			e.printStackTrace();
		}

		try {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(new Date());
			if (calendar.get(Calendar.HOUR_OF_DAY) > 3 || (calendar.get(Calendar.HOUR_OF_DAY) == 3 && calendar.get(Calendar.MINUTE) >= 45))
				calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
			calendar.set(Calendar.HOUR_OF_DAY, 3);
			calendar.set(Calendar.MINUTE, 45);
			Date sched = calendar.getTime();

			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Le réseau redémarrera à 4h du matin soit dans précisément 15 minutes.");
				}
			}, sched);
			this.getLogger().info("Scheduled automatic reboot at : " + calendar.toString());
		} catch (Exception e) {
			this.getLogger().severe("CANNOT SCHEDULE AUTOMATIC SHUTDOWN.");
			e.printStackTrace();
		}

		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
			citiesManager.payTaxes(false);
	}

	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	public CitiesManager getCitiesManager() {
		return citiesManager;
	}

	public JobsManager getJobsManager() {
		return jobsManager;
	}

	public TransactionsHelper getTransactionsHelper() {
		return transactionsHelper;
	}

	public EconomyManager getEconomyManager() {
		return economyManager;
	}

	public ShopsManager getShopsManager() {
		return shopsManager;
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}
}
