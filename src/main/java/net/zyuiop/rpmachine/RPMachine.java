package net.zyuiop.rpmachine;

import com.google.common.collect.Lists;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.SelectionManager;
import net.zyuiop.rpmachine.cities.commands.CityCommand;
import net.zyuiop.rpmachine.cities.commands.CommandBypass;
import net.zyuiop.rpmachine.cities.commands.CommandRuntaxes;
import net.zyuiop.rpmachine.cities.commands.PlotCommand;
import net.zyuiop.rpmachine.cities.listeners.CitiesListener;
import net.zyuiop.rpmachine.commands.*;
import net.zyuiop.rpmachine.database.DatabaseManager;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.shops.ShopsManager;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.economy.TransactionsHelper;
import net.zyuiop.rpmachine.economy.commands.CommandJob;
import net.zyuiop.rpmachine.economy.commands.CommandMoney;
import net.zyuiop.rpmachine.economy.commands.CommandPay;
import net.zyuiop.rpmachine.shops.CommandShops;
import net.zyuiop.rpmachine.economy.jobs.JobsManager;
import net.zyuiop.rpmachine.economy.listeners.PlayerListener;
import net.zyuiop.rpmachine.shops.SignsListener;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.gui.WindowsListener;
import net.zyuiop.rpmachine.projects.ProjectCommand;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import net.zyuiop.rpmachine.scoreboards.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class RPMachine extends JavaPlugin {

    private static RPMachine instance;
    private DatabaseManager databaseManager;
    private TransactionsHelper transactionsHelper;
    private EconomyManager economyManager;
    private JobsManager jobsManager;
    private CitiesManager citiesManager;
    private SelectionManager selectionManager;
    private ScoreboardManager scoreboardManager;
    private ProjectsManager projectsManager;
    private ShopsManager shopsManager;

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

    public static LegalEntity getPlayerActAs(Player player) {
        return getPlayerRoleToken(player).getLegalEntity();
    }

    public static void setPlayerRoleToken(Player player, LegalEntity token) {
        player.setMetadata("roleToken", new FixedMetadataValue(RPMachine.getInstance(), new RoleToken(player, token)));
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
        this.economyManager = new EconomyManager();
        this.transactionsHelper = new TransactionsHelper(this.economyManager);
        this.jobsManager = new JobsManager(this);
        this.citiesManager = new CitiesManager(this);
        this.selectionManager = new SelectionManager(citiesManager);
        this.scoreboardManager = new ScoreboardManager(this);
        this.projectsManager = new ProjectsManager(this);

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

        ItemStack capturator = new ItemStack(Material.SPAWNER);
        ItemMeta meta = capturator.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "PokeBall");
        meta.setLore(Lists.newArrayList(ChatColor.GRAY + "Clic droit sur un animal pour le capturer !", ChatColor.RED + "Attention !", ChatColor.RED + "Les données de l'entité ne sont pas conservées"));
        capturator.setItemMeta(meta);
        ShapedRecipe recipe = new ShapedRecipe(capturator);
        recipe.shape("XXX", "XCX", "XXX");
        recipe.setIngredient('X', Material.IRON_BARS);
        recipe.setIngredient('C', Material.CHEST);

        Bukkit.addRecipe(recipe);

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

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ProjectsManager getProjectsManager() {
        return projectsManager;
    }
}
