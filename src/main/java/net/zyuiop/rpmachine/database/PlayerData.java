package net.zyuiop.rpmachine.database;

import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.cities.LandOwner;
import net.zyuiop.rpmachine.economy.AccountHolder;
import net.zyuiop.rpmachine.economy.TaxPayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author zyuiop
 */
public interface PlayerData extends TaxPayer, LandOwner {
	String getJob();

	void setJob(String job);

	VirtualLocation getHome();

	void setHome(VirtualLocation location);

	boolean isNew();

	UUID getUuid();

	@Override
	default boolean canManage(Player player) {
		return player.getUniqueId().equals(getUuid());
	}

	boolean togglePlotMessages();
}
