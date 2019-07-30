package net.zyuiop.rpmachine.economy;

import org.bukkit.entity.Player;

/**
 * @author zyuiop
 */
@Deprecated
public interface ShopOwner {
	/**
	 * Called when a player tries to manage a shop
	 * @param player The player trying to interract
	 * @return true if the player can manage the shop
	 */
	boolean canManageShop(Player player);
}
