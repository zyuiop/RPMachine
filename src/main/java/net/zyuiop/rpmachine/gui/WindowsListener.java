package net.zyuiop.rpmachine.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * @author Louis Vialar
 */
public class WindowsListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryInteract(InventoryClickEvent event) {
        Window window = Window.getOpenWindow(event.getWhoClicked());

        if (window != null) {
            if (event.getClickedInventory().equals(window.inventory))
                window.clickPosition(event.getSlot());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryInteract(InventoryCloseEvent event) {
        if (Window.getOpenWindow(event.getPlayer()) != null)
            Window.getOpenWindow(event.getPlayer()).close();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryInteract(InventoryDragEvent event) {
        if (Window.getOpenWindow(event.getWhoClicked()) != null)
            event.setCancelled(true);
    }


}
