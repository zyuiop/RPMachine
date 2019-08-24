package net.zyuiop.rpmachine.common.listeners;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.common.selections.PlayerSelection;
import net.zyuiop.rpmachine.common.selections.Selection;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SelectionListener implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType().equals(Material.STICK)) {
            Selection selection = PlayerSelection.getPlayerSelection(player);

            if (selection == null) {
                City city = RPMachine.getInstance().getCitiesManager().getPlayerCity(player.getUniqueId());
                if (city == null || (!city.getMayor().equals(player.getUniqueId()) && !city.getCouncils().contains(player.getUniqueId())))
                    return;

                selection = PlayerSelection.getOrCreatePlayerSelection(player);
            }

            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                selection.rightClick(event.getClickedBlock(), player);
                event.setCancelled(true);
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                selection.leftClick(event.getClickedBlock(), player);
                event.setCancelled(true);
            }
        }
    }
}
