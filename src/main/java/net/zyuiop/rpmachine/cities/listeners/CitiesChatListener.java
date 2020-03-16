package net.zyuiop.rpmachine.cities.listeners;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class CitiesChatListener implements Listener {
    private final CitiesManager cm;

    public CitiesChatListener(CitiesManager cm) {
        this.cm = cm;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent ev) {
        var c = this.cm.getPlayerCity(ev.getPlayer());
        if (c != null) {

            if (ev.getMessage().startsWith("!")) {

                ev.getRecipients().clear();
                ev.getRecipients().addAll(c.getOnlineInhabitants());

                ev.setFormat(c.getChatColor() + "(Chat de vill - " + c.getCityName() + ") " + ChatColor.GRAY + ChatColor.ITALIC + "%s: %s");
            } else {
                ev.setFormat(c.getChatColor() + "[" + c.getCityName() + "]" + ChatColor.RESET + ev.getFormat());
            }
        }
    }
}
