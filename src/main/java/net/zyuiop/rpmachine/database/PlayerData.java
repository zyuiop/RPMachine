package net.zyuiop.rpmachine.database;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * @author zyuiop
 */
public interface PlayerData extends LegalEntity {
	String getJob();

	void setJob(String job);

	VirtualLocation getHome();

	void setHome(VirtualLocation location);

	boolean isNew();

	UUID getUuid();

	default String getName() {
		return RPMachine.database().getUUIDTranslator().getName(getUuid());
	}

	boolean togglePlotMessages();

	void setAttribute(String key, Object value);

	<T> T getAttribute(String key);

	boolean hasAttribute(String key);

	@Override
	default boolean hasDelegatedPermission(Player player, DelegatedPermission permission) {
		return true; // Player has all permissions on properties he manages!
	}
}
