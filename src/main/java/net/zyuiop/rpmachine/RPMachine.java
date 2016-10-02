package net.zyuiop.rpmachine;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.SelectionManager;
import net.zyuiop.rpmachine.cities.commands.*;
import net.zyuiop.rpmachine.cities.listeners.CitiesListener;
import net.zyuiop.rpmachine.database.DatabaseManager;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.database.ShopsManager;
import net.zyuiop.rpmachine.economy.TransactionsHelper;
import net.zyuiop.rpmachine.economy.commands.*;
import net.zyuiop.rpmachine.economy.jobs.JobsManager;
import net.zyuiop.rpmachine.economy.listeners.PlayerListener;
import net.zyuiop.rpmachine.economy.listeners.SignsListener;
import net.zyuiop.rpmachine.economy.shops.AbstractShopSign;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RPMachine extends JavaPlugin {

	private static RPMachine instance;
	private DatabaseManager databaseManager; // TODO : init
	private TransactionsHelper transactionsHelper;
	private EconomyManager economyManager;
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

		saveDefaultConfig();
		if (!loadDatabase()) {
			setEnabled(false);
			return;
		}

		this.economyManager = new EconomyManager();
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
		getCommand("myshops").setExecutor(new CommandShops(this));

		Bukkit.getPluginManager().registerEvents(selectionManager, this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
		Bukkit.getPluginManager().registerEvents(new SignsListener(this), this);
		Bukkit.getPluginManager().registerEvents(new CitiesListener(citiesManager), this);

		Bukkit.getScheduler().runTaskTimer(this, () -> {
			Bukkit.getLogger().info("Saving world...");
			Bukkit.getWorld("world").save();
			Bukkit.getLogger().info("Done !");
		}, 20 * 60, 20 * 20 * 60);

		Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				PlayerData data = databaseManager.getPlayerData(player.getUniqueId());
				if (data.getJob() == null)
					player.sendMessage(ChatColor.GOLD + "Vous n'avez pas encore choisi de métier. Tapez /job pour plus d'infos.");
			}
		}, 100L, 3 * 60 * 20L);

		ItemStack capturator = new ItemStack(Material.MOB_SPAWNER);
		ItemMeta meta = capturator.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "PokeBall");
		meta.setLore(Lists.newArrayList(ChatColor.GRAY + "Clic droit sur un animal pour le capturer !", ChatColor.RED + "Attention !", ChatColor.RED + "Les données de l'entité ne sont pas conservées"));
		capturator.setItemMeta(meta);
		ShapedRecipe recipe = new ShapedRecipe(capturator);
		recipe.shape("XXX", "XCX", "XXX");
		recipe.setIngredient('X', Material.IRON_FENCE);
		recipe.setIngredient('C', Material.CHEST);

		Bukkit.addRecipe(recipe);

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
					Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "Le serveur redémarrera à 4h du matin soit dans précisément 15 minutes.");
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

	private boolean loadDatabase() {
		String managerClass = getConfig().getString("database", "net.zyuiop.rpmachine.database.bukkitbridge.BukkitBridgeDatabase");
		try {
			Class<? extends DatabaseManager> clazz = (Class<? extends DatabaseManager>) Class.forName(managerClass);
			databaseManager = clazz.newInstance();
			return true;
		} catch (ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException e) {
			getLogger().severe("Cannot load Database Manager. Cancelling start.");
			e.printStackTrace();
			return false;
		}
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
		return getDatabaseManager().getShopsManager();
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public DatabaseManager getDatabaseManager() {
		return databaseManager;
	}

	public static DatabaseManager database() {
		return getInstance().getDatabaseManager();
	}
}
