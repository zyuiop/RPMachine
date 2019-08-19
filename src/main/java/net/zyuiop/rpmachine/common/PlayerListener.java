package net.zyuiop.rpmachine.common;

import com.destroystokyo.paper.event.entity.PhantomPreSpawnEvent;
import net.md_5.bungee.api.ChatColor;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.utils.ConfigFunction;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.util.Date;
import java.util.function.DoubleFunction;

public class PlayerListener implements Listener {
    private static final String ATTRIBUTE_LAST_DAILY_WAGE = "lastDailyWage";
    private final RPMachine plugin;

    private final DoubleFunction<Double> dailyWageFunc;

    public PlayerListener(RPMachine plugin) {
        this.plugin = plugin;

        Configuration c = plugin.getConfig();
        dailyWageFunc = ConfigFunction.getFunction(c.getConfigurationSection("dailyWage"), x -> Math.min(100, Math.max(5, x * 0.01D)));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        plugin.getScoreboardManager().addPlayer(event.getPlayer());

        PlayerData d = RPMachine.database().getPlayerData(event.getPlayer().getUniqueId());
        if (d.isNew()) {
            d.setBalance(RPMachine.getCreationBalance());
            event.setJoinMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "Bienvenue à " + event.getPlayer().getDisplayName() + ChatColor.YELLOW + "" + ChatColor.ITALIC + " !");
            Messages.credit(event.getPlayer(), RPMachine.getCreationBalance(), "cadeau de bienvenue");
            event.getPlayer().teleport(event.getPlayer().getWorld().getSpawnLocation()); // tp au spawn
            event.getPlayer().getInventory().addItem(new ItemStack(Material.COOKED_BEEF, 16));
        } else {
            event.setJoinMessage(ChatColor.YELLOW + event.getPlayer().getDisplayName() + ChatColor.GRAY + "" + ChatColor.ITALIC + " vient de se connecter.");
            RPMachine.getInstance().getJobsManager().checkPlayerJob(event.getPlayer(), d);
        }

        String date = DateFormat.getDateInstance().format(new Date());
        if (!d.hasAttribute(ATTRIBUTE_LAST_DAILY_WAGE) || !d.getAttribute(ATTRIBUTE_LAST_DAILY_WAGE).equals(date)) {
            d.setAttribute(ATTRIBUTE_LAST_DAILY_WAGE, date);
            double amt = dailyWageFunc.apply(d.getBalance());
            d.creditMoney(amt);
            Messages.credit(event.getPlayer(), amt, "première connexion du jour");
            d.resetCollectedItems();
        }

        RPMachine.database().getUUIDTranslator().cachePair(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    @EventHandler
    public void phantomSpawn(PhantomPreSpawnEvent e) {
        e.setCancelled(true); // end this please !
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        plugin.getScoreboardManager().removePlayer(event.getPlayer());
        event.setQuitMessage(ChatColor.YELLOW + event.getPlayer().getDisplayName() + ChatColor.GRAY + "" + ChatColor.ITALIC + " vient de se déconnecter.");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() == Material.SPAWNER) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void autoFarmsBlocker(EntityDeathEvent entityDeathEvent) {
        if (entityDeathEvent.getEntityType() != EntityType.PLAYER) {
            entityDeathEvent.getDrops()
                    .removeIf(stack ->
                            (stack.getType() == Material.IRON_INGOT && entityDeathEvent.getEntity().getType() == EntityType.IRON_GOLEM) ||
                                    stack.getType() == Material.GOLD_NUGGET);
        }
    }
}
