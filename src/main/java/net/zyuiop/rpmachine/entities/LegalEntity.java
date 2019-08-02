package net.zyuiop.rpmachine.entities;

import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A legal entity is an abstraction for anything that can own goods, lands, shops, pay taxes, ...
 * @author zyuiop
 */
public interface LegalEntity extends AccountHolder {
	void setUnpaidTaxes(String city, double amount);

	double getUnpaidTaxes(String city);

	void setLastTaxes(String city, Date date);

	Date getLastTaxes(String city);

	Map<String, Double> getUnpaidTaxes();

	/**
	 * Check if the given player has the right to execute the given command as the current taxpayer
	 * @param player the player to check
	 * @param permission the permission to check
	 * @return true if the command is allowed, false if not
	 */
	boolean hasDelegatedPermission(@Nonnull Player player, @Nonnull DelegatedPermission permission);

	default String tag() {
		LegalEntityType type = LegalEntityType.get(this);
		String tagEnd = type.holder().repository.get().getTag(this);

		return type.name() + "::" + tagEnd;
	}

	static LegalEntity getEntity(@Nullable String tag) {
		if (tag == null)
			return null;

		String[] parts = tag.split("::");
		LegalEntityType type = LegalEntityType.valueOf(parts[0]);
		return type.holder().repository.get().findEntity(StringUtils.join(parts, "::", 1, parts.length));
	}

	/**
	 * Returns a description of this entity
	 */
	String displayable();

	/**
	 * Returns a very short (<= 16 c) or this entity
	 */
	String shortDisplayable();

	/**
	 * Returns a list of players that can administrate this entity
	 */
	Set<UUID> getAdministrators();

	default Set<Player> getOnlineAdministrators() {
		return getAdministrators().stream()
				.map(Bukkit::getPlayer)
				.filter(Objects::nonNull)
				.filter(OfflinePlayer::isOnline)
				.collect(Collectors.toSet());
	}
}
