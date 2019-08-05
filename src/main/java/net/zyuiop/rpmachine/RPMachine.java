package net.zyuiop.rpmachine;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.SelectionManager;
import net.zyuiop.rpmachine.cities.commands.CityCommand;
import net.zyuiop.rpmachine.cities.commands.CommandBypass;
import net.zyuiop.rpmachine.cities.commands.CommandRuntaxes;
import net.zyuiop.rpmachine.cities.commands.PlotCommand;
import net.zyuiop.rpmachine.cities.listeners.CitiesListener;
import net.zyuiop.rpmachine.commands.*;
import net.zyuiop.rpmachine.common.PlayerHeadCraft;
import net.zyuiop.rpmachine.common.PlayerListener;
import net.zyuiop.rpmachine.database.DatabaseManager;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.gui.WindowsListener;
import net.zyuiop.rpmachine.jobs.CommandJob;
import net.zyuiop.rpmachine.jobs.JobsManager;
import net.zyuiop.rpmachine.multiverse.MultiverseManager;
import net.zyuiop.rpmachine.multiverse.MultiverseWorld;
import net.zyuiop.rpmachine.projects.ProjectCommand;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import net.zyuiop.rpmachine.scoreboards.ScoreboardManager;
import net.zyuiop.rpmachine.shops.CommandShops;
import net.zyuiop.rpmachine.shops.ShopsManager;
import net.zyuiop.rpmachine.shops.SignsListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RPMachine extends JavaPlugin {

    private static RPMachine instance;
    private static String moneyName = null;
    private static double baseAmount = -1;
    private DatabaseManager databaseManager;
    private JobsManager jobsManager;
    private CitiesManager citiesManager;
    private SelectionManager selectionManager;
    private ScoreboardManager scoreboardManager;
    private ProjectsManager projectsManager;
    private ShopsManager shopsManager;
    private MultiverseManager multiverseManager;

    public static RPMachine getInstance() {
        return instance;
    }

    public static DatabaseManager database() {
        return getInstance().getDatabaseManager();
    }

    public static RoleToken getPlayerRoleToken(Player player) {
        for (MetadataValue value : player.getMetadata("roleToken"))
            if (value.getOwningPlugin().equals(RPMachine.getInstance()))
                return (RoleToken) value.value();

        setPlayerRoleToken(player, database().getPlayerData(player.getUniqueId()));
        return getPlayerRoleToken(player);
    }

    public static List<Player> getPlayersActingAs(String tag) {
        return Bukkit.getOnlinePlayers().stream()
                .filter(pl -> getPlayerRoleToken(pl).getTag().equals(tag))
                .collect(Collectors.toList());
    }

    public static LegalEntity getPlayerActAs(Player player) {
        return getPlayerRoleToken(player).getLegalEntity();
    }

    public static void setPlayerRoleToken(Player player, LegalEntity token) {
        player.setMetadata("roleToken", new FixedMetadataValue(RPMachine.getInstance(), new RoleToken(player, token)));
    }

    public static String getCurrencyName() {
        if (moneyName == null) {
            moneyName = getInstance().getConfig().getString("money.symbol", "$");
        }
        return moneyName;
    }

    public static double getCreationBalance() {
        if (baseAmount == -1) {
            baseAmount = getInstance().getConfig().getDouble("money.baseAmount", 150D);
        }
        return baseAmount;
    }

    public static boolean isTpEnabled() {
        return getInstance().getConfig().getBoolean("cityTp.enable", false);
    }

    public MultiverseManager getMultiverseManager() {
        return multiverseManager;
    }

    @Override
    public void onEnable() {
        try {
            start();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error while starting, killing server", e);
            Bukkit.shutdown();
        }
    }

    private void start() {
        getLogger().info("Begin RPMachine enable...");
        instance = this;

        saveDefaultConfig();

        // Load useful and non db dependent managers
        this.jobsManager = new JobsManager(this);
        this.citiesManager = new CitiesManager(this);
        this.selectionManager = new SelectionManager(citiesManager);
        this.scoreboardManager = new ScoreboardManager(this);
        this.projectsManager = new ProjectsManager(this);
        this.multiverseManager = new MultiverseManager();

        // Load DB
        if (!loadDatabase()) {
            getLogger().severe("Cannot load DB, shutting down.");
            Bukkit.shutdown();
            return;
        }

        this.shopsManager = new ShopsManager();

        // Auto-registering commands
        new CityCommand(citiesManager);
        new PlotCommand(citiesManager);
        new ProjectCommand(projectsManager);
        new CommandInventory(); // both invsee and endsee
        new CommandPay();
        new CommandActAs();
        new CommandShops();
        new CommandMoney();
        new CommandHome();
        new CommandJob();

        // Classic commands
        getCommand("fly").setExecutor(new CommandFly());
        getCommand("sethome").setExecutor(new CommandSethome());
        getCommand("spawn").setExecutor(new CommandSpawn());
        getCommand("info").setExecutor(new CommandHelp());
        getCommand("runtaxes").setExecutor(new CommandRuntaxes(citiesManager));
        getCommand("bypass").setExecutor(new CommandBypass(citiesManager));

        Bukkit.getPluginManager().registerEvents(selectionManager, this);
        Bukkit.getPluginManager().registerEvents(new WindowsListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new SignsListener(), this);
        Bukkit.getPluginManager().registerEvents(new CitiesListener(citiesManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerHeadCraft(), this);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Bukkit.getLogger().info("Saving world...");
            Bukkit.getWorld("world").save();
            Bukkit.getLogger().info("Done !");
        }, 20 * 60, 20 * 20 * 60);

        // Create resources world
        this.multiverseManager.generateWorlds();

        scheduleReboot();

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
            citiesManager.payTaxes(false);
    }

    private void scheduleReboot() {
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

            calendar.set(Calendar.HOUR_OF_DAY, 4);
            calendar.set(Calendar.MINUTE, 0);
            sched = calendar.getTime();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    multiverseManager.onShutdown(true);

                    Bukkit.getServer().shutdown();
                }
            }, sched);
            this.getLogger().info("Scheduled automatic reboot at : " + calendar.toString());
        } catch (Exception e) {
            this.getLogger().severe("CANNOT SCHEDULE AUTOMATIC SHUTDOWN.");
            e.printStackTrace();
        }
    }

    private boolean loadDatabase() {
        String managerClass = getConfig().getString("database", "net.zyuiop.rpmachine.database.filestorage.FileStorageDatabase");
        try {
            Class<? extends DatabaseManager> clazz = (Class<? extends DatabaseManager>) Class.forName(managerClass);
            databaseManager = clazz.newInstance();
            databaseManager.load();
            return true;
        } catch (ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException | IOException e) {
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

    public ShopsManager getShopsManager() {
        return shopsManager;
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling: deleting resources world");
        this.multiverseManager.onShutdown(false);

        super.onDisable();
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ProjectsManager getProjectsManager() {
        return projectsManager;
    }
}
