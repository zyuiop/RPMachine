package net.zyuiop.rpmachine.common.listeners;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * @author Louis Vialar
 */
public class MendingListener implements Listener {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEnchant(EnchantItemEvent event) {
        if (event.getEnchantsToAdd().containsKey(Enchantment.MENDING)) {
            event.setCancelled(true);
            event.getEnchanter().sendMessage(ChatColor.RED + "L'enchantement MENDING est désactivé.");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onEnchant(PrepareAnvilEvent event) {
        if (event.getResult().getEnchantments().containsKey(Enchantment.MENDING)) {
            event.setResult(null);
            event.getViewers().forEach(e -> e.sendMessage(ChatColor.RED + "L'enchantement MENDING est désactivé."));
        }
    }

    private void removeMending(ItemStack item, Player player) {
        item.removeEnchantment(Enchantment.MENDING);
        player.sendMessage(ChatColor.RED + "Désolé, l'enchantement MENDING a été banni du serveur. Il a été supprimé de votre " + item.getType());
        RPMachine.getInstance().getDatabaseManager().getPlayerData(player).creditMoney(1000);
        Messages.credit(player, 1000, "Suppression de MENDING sur " + item.getType());

        Bukkit.getLogger().info("MENDING: Removed enchant on " + item + " of player " + player.getName() + " " + player.getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Arrays.spliterator(event.getPlayer().getInventory().getContents()).forEachRemaining(item -> {
            if (item == null) return;
            if (item.getEnchantments().containsKey(Enchantment.MENDING)) {
                removeMending(item, event.getPlayer());
            }
        });

        Arrays.spliterator(event.getPlayer().getEnderChest().getContents()).forEachRemaining(item -> {
            if (item == null) return;
            if (item.getEnchantments().containsKey(Enchantment.MENDING)) {
                removeMending(item, event.getPlayer());
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTake(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getClickedBlock().getState() instanceof Container) {
            for (ItemStack s : ((Container) event.getClickedBlock().getState()).getInventory().getContents()) {
                if (s == null)
                    continue;

                if (s.getEnchantments().containsKey(Enchantment.MENDING)) {
                    removeMending(s, event.getPlayer());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack s = event.getItemDrop().getItemStack();

        if (s.getEnchantments().containsKey(Enchantment.MENDING)) {
            removeMending(s, event.getPlayer());
        }

    }
}
