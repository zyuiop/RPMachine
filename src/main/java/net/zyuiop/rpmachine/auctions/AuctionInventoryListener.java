package net.zyuiop.rpmachine.auctions;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Louis Vialar
 */
public class AuctionInventoryListener implements Listener {
    public static final AuctionInventoryListener INSTANCE = new AuctionInventoryListener();

    private AuctionInventoryListener() {}

    private final Map<UUID, Runnable> callbacks = new HashMap<>();

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Runnable r = callbacks.remove(event.getPlayer().getUniqueId());

        if (r != null)
            r.run();
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        Runnable r = callbacks.remove(event.getPlayer().getUniqueId());

        if (r != null)
            r.run();
    }

    public void addPlayer(Player player, Runnable callback) {
        Runnable r = callbacks.remove(player.getUniqueId());

        if (r != null)
            r.run();

        callbacks.put(player.getUniqueId(), callback);
    }
}
