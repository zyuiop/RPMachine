package net.zyuiop.rpmachine.cities;

import org.bukkit.entity.Player;

/**
 * @author zyuiop
 */
@Deprecated
public interface LandOwner {
	/**
	 * Called when a player tries to manage a plot (add / remove members, etc)
	 * @param player The player trying to interract
	 * @return true if the player can manage the plot
	 */
	boolean canManagePlot(Player player);
}
