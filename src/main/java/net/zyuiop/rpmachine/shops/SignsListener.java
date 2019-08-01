package net.zyuiop.rpmachine.shops;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.shops.types.AbstractShopSign;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignsListener implements Listener {
    @EventHandler
    public void onSignPlace(SignChangeEvent event) {
        if (!event.getPlayer().getWorld().getName().equals("world"))
            return;

        RPMachine.getInstance().getShopsManager().buildShop(event);
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null || !isSign(event.getClickedBlock()))
            return;

        AbstractShopSign sign = RPMachine.getInstance().getShopsManager().get(event.getClickedBlock().getLocation());
        if (sign != null) {
            if (event.getPlayer().isSneaking() && event.getPlayer().hasPermission("sign.debug"))
                sign.debug(event.getPlayer());
            else
                sign.rightClick(event.getPlayer(), event);
        }
    }

    private boolean isSign(Block block) {
        BlockData data = block.getBlockData();

        return data instanceof Sign || data instanceof WallSign;
    }

    private void tryBreakSign(BlockBreakEvent event, Block block) {
        event.setCancelled(true);
        AbstractShopSign sign = RPMachine.getInstance().getShopsManager().get(block.getLocation());
        if (sign == null)
            event.setCancelled(false);
        else
            event.setCancelled(!sign.breakSign(event.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        if (isSign(event.getBlock())) {
            tryBreakSign(event, event.getBlock());
        } else {
            for (BlockFace face : BlockFace.values()) {
                Block block = event.getBlock().getRelative(face);
                if (block.isEmpty())
                    continue;

                if (isSign(block)) {
                    tryBreakSign(event, block);
                }
            }
        }
    }
}
