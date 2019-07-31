package net.zyuiop.rpmachine.economy;

import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Map;

/**
 * @author zyuiop
 */
public interface TaxPayer extends AccountHolder {
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
}
