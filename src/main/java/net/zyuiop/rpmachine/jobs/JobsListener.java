package net.zyuiop.rpmachine.jobs;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.function.Function;

/**
 * @author Louis Vialar
 */
public class JobsListener implements Listener {
    private final JobsManager manager;

    public JobsListener(JobsManager manager) {
        this.manager = manager;
    }

    private void checkCraft(ItemStack crafted, HumanEntity crafter, Cancellable event) {
        if (crafter instanceof Player && crafted != null) {
            Player p = (Player) crafter;
            Material m = crafted.getType();
            if (!manager.isFreeToCraft(m)) {
                if (!manager.canCraft(p, m)) {
                    manager.printAvailableJobsToCraft(m, p);
                    event.setCancelled(true);
                }
            }
        }
    }

    private void checkBlockPlaceOrUse(Block block, HumanEntity crafter, Cancellable event) {
        if (crafter instanceof Player && block != null) {
            Player p = (Player) crafter;
            Material m = block.getType();
            if (!manager.isFreeToUse(m)) {
                if (!manager.canUse(p, m)) {
                    manager.printAvailableJobsToUse(m, p);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        ItemStack crafted = event.getRecipe().getResult();
        HumanEntity crafter = event.getWhoClicked();

        checkCraft(crafted, crafter, event);
    }

    // Potions crafting is hardly interruptable
    // Same for furnace and a lot of crafts actually
    // Better luck on the blocks interruption I think

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        checkBlockPlaceOrUse(event.getBlockPlaced(), event.getPlayer(), event);
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK)
            return; // left click can be break

        checkBlockPlaceOrUse(event.getClickedBlock(), event.getPlayer(), event);
    }

    @EventHandler
    public void onBreak(BlockDropItemEvent event) {
        if (RPMachine.getInstance().getCitiesManager().getCityHere(event.getBlock().getChunk()) != null)
            return;
        if (RPMachine.getInstance().getProjectsManager().getZoneHere(event.getBlock().getLocation()) != null)
            return;


        Player p = event.getPlayer();
        checkItems(p, event.getItems(), Item::getItemStack);
    }

    private <T> void checkItems(Player p, Iterable<T> drops, Function<T, ItemStack> converter) {
        Iterator<T> stacks = drops.iterator();
        PlayerData data = RPMachine.getInstance().getDatabaseManager().getPlayerData(p);
        while (stacks.hasNext()) {
            ItemStack stack = converter.apply(stacks.next());
            Material m = stack.getType();

            int limit = manager.getCollectLimit(m);
            if (limit >= 0) {
                if (!manager.canCollect(p, m)) {

                    if (data.getCollectedItems(m) + stack.getAmount() > limit) {
                        int newAmt = limit - data.getCollectedItems(m);

                        if (newAmt <= 0)
                            stacks.remove();
                        else {
                            stack.setAmount(newAmt);
                            data.addCollectedItems(m, newAmt);
                        }

                        manager.printAvailableJobsToCollect(m, p, limit);
                    } else {
                        data.addCollectedItems(m, stack.getAmount());
                    }
                }
            }
        }
    }


    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (event.getEntity() instanceof Player)
            return; // Don't remove loots in PvP

        Player p = event.getEntity().getKiller();

        if (p != null) {
            checkItems(p, event.getDrops(), u -> u);
        } else {
            // Remove loots
            Iterator<ItemStack> stacks = event.getDrops().iterator();
            while (stacks.hasNext()) {
                ItemStack stack = stacks.next();
                Material m = stack.getType();

                int limit = manager.getCollectLimit(m);
                if (limit > 0)
                    stacks.remove();
            }
        }
    }
}
