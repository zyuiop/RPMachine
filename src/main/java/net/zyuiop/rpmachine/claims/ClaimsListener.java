package net.zyuiop.rpmachine.claims;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.data.type.Sign;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

/**
 * @author Louis Vialar
 */
public class ClaimsListener implements Listener {
    private static final String FIRE_STARTER_KEY = "fireStarter";
    private final Claims claims;

    public ClaimsListener(Claims claims) {
        this.claims = claims;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        //if (event.getAction() == Action.RIGHT_CLICK_AIR)
        //	return;

        if (event.getAction() == Action.PHYSICAL) {
            event.setCancelled(!claims.canInteractWithBlock(event.getPlayer(), event.getClickedBlock(), event.getAction()));
            return;
        }

        if (event.getItem() != null) {
            Material type = event.getItem().getType();
            if (type == Material.BUCKET || type == Material.WATER_BUCKET || type == Material.LAVA_BUCKET || type == Material.FLINT_AND_STEEL) {
                event.setCancelled(!claims.canBuild(event.getPlayer(), event.getClickedBlock().getLocation()));
                return;
            }
        }

        if (event.getClickedBlock() == null || !event.getClickedBlock().getType().isInteractable())
            return;

        if (event.getClickedBlock().getBlockData() instanceof Sign)
            return; // Signs are always interactable

        event.setCancelled(!claims.canInteractWithBlock(event.getPlayer(), event.getClickedBlock(), event.getAction()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = RPMachine.database().getPlayerData(player.getUniqueId());
        for (Map.Entry<String, Double> entry : data.getUnpaidTaxes().entrySet()) {
            double topay = entry.getValue();
            if (topay <= 0)
                continue;

            player.sendMessage(ChatColor.RED + "ATTENTION ! Votre compte ne contient pas assez d'argent pour payer vos impots.");
            player.sendMessage(ChatColor.RED + "Vous devez " + ChatColor.AQUA + topay + ChatColor.RED + " à la ville de " + ChatColor.AQUA + entry.getKey());
            player.sendMessage(ChatColor.RED + "Payez les rapidement avec " + ChatColor.AQUA + "/city paytaxes " + entry.getKey());
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        event.setCancelled(!claims.canBuild(event.getPlayer(), event.getBlock().getLocation()));
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent entityExplodeEvent) {
        entityExplodeEvent.blockList().clear();
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent explodeEvent) {
        explodeEvent.blockList().clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        event.setCancelled(!claims.canBuild(event.getPlayer(), event.getBlock().getLocation()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        event.setCancelled(!claims.canInteractWithEntity(event.getPlayer(), event.getRightClicked()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractAtEntityEvent event) {
        event.setCancelled(!claims.canInteractWithEntity(event.getPlayer(), event.getRightClicked()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFillBucket(PlayerBucketFillEvent event) {
        event.setCancelled(!claims.canBuild(event.getPlayer(), event.getBlockClicked().getLocation()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEmptyBucket(PlayerBucketEmptyEvent event) {
        event.setCancelled(!claims.canBuild(event.getPlayer(), event.getBlockClicked().getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHangingEntity(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player)
            event.setCancelled(!claims.canBuild((Player) event.getRemover(), event.getEntity().getLocation()));
        else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && !(event.getEntity() instanceof Monster))
            event.setCancelled(!claims.canDamageEntity((Player) event.getDamager(), event.getEntity()));
        else if (event.getDamager() instanceof Monster && event.getEntity() instanceof Hanging)
            event.setCancelled(true);
    }


    @EventHandler
    public void onFireSpread(BlockSpreadEvent ev) {
        if (ev.getSource().getType() == Material.FIRE) {
            if (ev.getBlock().hasMetadata(FIRE_STARTER_KEY)) {
                Player p = ev.getBlock().getMetadata(FIRE_STARTER_KEY).stream()
                        .filter(r -> r.getOwningPlugin().equals(RPMachine.getInstance()))
                        .map(r -> (Player) r.value())
                        .findFirst()
                        .orElse(null);

                if (p == null) {
                    ev.setCancelled(claims.isClaimed(ev.getBlock().getLocation()));
                } else {
                    ev.setCancelled(claims.canBuild(p, ev.getBlock().getLocation()));
                    ev.getBlock().setMetadata(FIRE_STARTER_KEY, new FixedMetadataValue(RPMachine.getInstance(), p));
                }
            } else {
                ev.setCancelled(claims.isClaimed(ev.getBlock().getLocation()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFireStart(BlockPlaceEvent ev) {
        if (ev.getBlock().getType() == Material.FIRE) {
            ev.getBlock().setMetadata(FIRE_STARTER_KEY, new FixedMetadataValue(RPMachine.getInstance(), ev.getPlayer()));
        }
    }

}
