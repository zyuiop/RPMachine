package net.zyuiop.rpmachine.jobs;

import net.zyuiop.rpmachine.RPMachine;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

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
            if (manager.isItemRestricted(m)) {
                if (!manager.isItemAllowed(p, m)) {
                    manager.printAvailableJobsForItem(m, p);
                    event.setCancelled(true);
                }
            } else if (manager.isBlockRestricted(m)) {
                if (!manager.isBlockAllowed(p, m)) {
                    manager.printAvailableJobsForBlock(m, p);
                    event.setCancelled(true);
                }
            }
        }
    }

    private void checkBlockPlaceOrUse(Block block, HumanEntity crafter, Cancellable event) {
        if (crafter instanceof Player && block != null) {
            Player p = (Player) crafter;
            Material m = block.getType();
            if (manager.isBlockRestricted(m)) {
                if (!manager.isBlockAllowed(p, m)) {
                    manager.printAvailableJobsForBlock(m, p);
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
}
