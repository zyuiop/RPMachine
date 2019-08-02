package net.zyuiop.rpmachine.economy.listeners;

import net.md_5.bungee.api.ChatColor;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.jobs.Job;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

import static org.bukkit.entity.EntityType.*;

@Deprecated // TODO Try to split this
public class PlayerListener implements Listener {
	private static final String ATTRIBUTE_LAST_DAILY_WAGE = "lastDailyWage";
	private final RPMachine plugin;

	private final int dailyWageMin;
	private final int dailyWageMax;
	private final double dailyWageRate;

	public PlayerListener(RPMachine plugin) {
		this.plugin = plugin;

		Configuration c = plugin.getConfig();
		dailyWageMin = c.getInt("dailyWage.min", 5);
		dailyWageMax = c.getInt("dailyWage.max", 100);
		dailyWageRate = c.getDouble("dailyWage.rate", 0.01D);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onJoin(PlayerJoinEvent event) {
		plugin.getScoreboardManager().addPlayer(event.getPlayer());

		PlayerData d = RPMachine.database().getPlayerData(event.getPlayer().getUniqueId());
		if (d.isNew()) {
			d.setBalance(RPMachine.getCreationBalance());
			event.setJoinMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "Bienvenue à " + event.getPlayer().getDisplayName() + ChatColor.YELLOW + "" + ChatColor.ITALIC + " !");
		}

		String date = DateFormat.getDateInstance().format(new Date());
		if (!d.hasAttribute(ATTRIBUTE_LAST_DAILY_WAGE) || !d.getAttribute(ATTRIBUTE_LAST_DAILY_WAGE).equals(date)) {
			d.setAttribute(ATTRIBUTE_LAST_DAILY_WAGE, date);
			double amt = Math.min(dailyWageMax, Math.max(dailyWageMin, d.getBalance() * dailyWageRate));
			d.creditMoney(amt);
			Messages.credit(event.getPlayer(), amt, "première connexion du jour");
		}

		RPMachine.database().getUUIDTranslator().cachePair(event.getPlayer().getUniqueId(), event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onLeave(PlayerQuitEvent event) {
		plugin.getScoreboardManager().removePlayer(event.getPlayer());
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.getBlockPlaced().getType() == Material.SPAWNER) {
			event.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		boolean mainHand = player.getInventory().getItemInOffHand().getType() == Material.AIR;
		ItemStack stack = mainHand ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
		if (stack.getType() == Material.SPAWNER) {
			Job job = RPMachine.getInstance().getJobsManager().getJob(player.getUniqueId());
			if (job != null && job.getJobName().equalsIgnoreCase("chasseur")) {
				EntityType type = event.getRightClicked().getType();
				if (type == COW || type == SHEEP || type == MUSHROOM_COW || type == HORSE || type == CHICKEN || type == PIG || type == RABBIT) {
					if (!(event.getRightClicked() instanceof Ageable) || ((Ageable) event.getRightClicked()).isAdult()) {
						event.getRightClicked().remove();
						player.sendMessage(ChatColor.GREEN + "Entité capturée !");

						Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(), () -> {
							SpawnEgg egg = new SpawnEgg(type);
							ItemStack hand = stack;
							if (hand.getAmount() > 1)
								hand.setAmount(hand.getAmount() - 1);
							else
								hand = null;

							if (mainHand)
								player.getInventory().setItemInOffHand(hand);
							else player.getInventory().setItemInMainHand(hand);

							player.getInventory().addItem(egg.toItemStack(1));
						}, 3);
					} else {
						player.sendMessage(ChatColor.RED + "Impossible de capturer un bébé !");
					}
				} else {
					player.sendMessage(ChatColor.RED + "Cette entité ne peut pas être capturée.");
				}
			} else {
				player.sendMessage(ChatColor.RED + "Seuls les chasseurs peuvent attraper des entités !");
			}
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent entityDeathEvent) {
		if (entityDeathEvent.getEntityType() != EntityType.PLAYER) {
			Iterator<ItemStack> iter = entityDeathEvent.getDrops().iterator();
			while (iter.hasNext()) {
				ItemStack stack = iter.next();
				if ((stack.getType() == Material.IRON_INGOT && entityDeathEvent.getEntity().getType() == EntityType.IRON_GOLEM) || stack.getType() == Material.GOLD_NUGGET)
					iter.remove();
			}
		}
	}

}
