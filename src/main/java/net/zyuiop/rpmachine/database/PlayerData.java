package net.zyuiop.rpmachine.database;

import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.cities.LandOwner;
import net.zyuiop.rpmachine.economy.ShopOwner;
import net.zyuiop.rpmachine.economy.TaxPayer;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author zyuiop
 */
public interface PlayerData extends TaxPayer, LandOwner, ShopOwner {
	String getJob();

	void setJob(String job);

	VirtualLocation getHome();

	void setHome(VirtualLocation location);

	boolean isNew();

	UUID getUuid();

	@Override
	default boolean canManagePlot(Player player) {
		return player.getUniqueId().equals(getUuid());
	}

	@Override
	default boolean canManageShop(Player player) {
		return player.getUniqueId().equals(getUuid());
	}

	boolean togglePlotMessages();

	@Override
	default boolean hasDelegatedPermission(Player player, DelegatedPermission permission) {
		return true; // Player has all permissions on properties he manages!
	}
}
