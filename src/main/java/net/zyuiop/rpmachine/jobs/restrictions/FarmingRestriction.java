package net.zyuiop.rpmachine.jobs.restrictions;

import com.google.common.collect.ImmutableSet;
import net.zyuiop.rpmachine.jobs.JobRestriction;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Set;

/**
 * @author Louis Vialar
 */
public class FarmingRestriction extends JobRestriction {
    private static final Set<Material> seeds = ImmutableSet.of(
            Material.BEETROOT_SEEDS, Material.MELON_SEEDS, Material.PUMPKIN_SEEDS, Material.WHEAT_SEEDS,
            Material.COCOA, Material.COCOA_BEANS, Material.SUGAR_CANE,
            Material.CARROT, Material.MELON_SEEDS,

            Material.MELON_STEM, Material.WHEAT, Material.MUSHROOM_STEM, Material.PUMPKIN_STEM
    );
    private static final Set<Material> hoes = ImmutableSet.of(
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE
    );
    private static final Set<Material> dirt = ImmutableSet.of(
            Material.DIRT, Material.COARSE_DIRT, Material.GRASS, Material.GRASS_BLOCK, Material.GRASS_PATH
    );

    @EventHandler
    public void onUseHoe(PlayerInteractEvent event) {
        if (event.getItem() != null && hoes.contains(event.getItem().getType())) {
            // is target block dirt ?
            if (event.getClickedBlock() != null && dirt.contains(event.getClickedBlock().getType())) {
                if (!isAllowed(event.getPlayer()))
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlantSeeds(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType().isEdible() || seeds.contains(event.getBlockPlaced().getType())) {
            if (!isAllowed(event.getPlayer()))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onFertilize(BlockFertilizeEvent event) {
        if (!isAllowed(event.getPlayer()))
            event.setCancelled(true);
    }
}
