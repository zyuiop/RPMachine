package net.zyuiop.rpmachine.common.selections;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Louis Vialar
 */
public class PlayerSelection {
    private static final Map<UUID, Selection> selections = new HashMap<>();

    public static Selection getPlayerSelection(Player player) {
        return selections.get(player.getUniqueId());
    }

    public static Selection getOrCreatePlayerSelection(Player player) {
        if (!selections.containsKey(player.getUniqueId()))
            selections.put(player.getUniqueId(), new RectangleSelection());
        return selections.get(player.getUniqueId());
    }

    public static void createSelection(Player player, Selection selection) {
        selections.put(player.getUniqueId(), selection);
    }
}
