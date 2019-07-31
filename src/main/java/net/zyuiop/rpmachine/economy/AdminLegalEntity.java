package net.zyuiop.rpmachine.economy;

import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.LegalEntityRepository;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zyuiop
 */
public class AdminLegalEntity implements LegalEntity, LegalEntityRepository<AdminLegalEntity> {
	public static final AdminLegalEntity INSTANCE = new AdminLegalEntity();

	private AdminLegalEntity() {

	}

	@Override
	public double getBalance() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void setBalance(double amount) {

	}

	@Override
	public boolean withdrawMoney(double amount) {
		return true;
	}

	@Override
	public void creditMoney(double amount) {

	}

	@Override
	public void setUnpaidTaxes(String city, double amount) {

	}

	@Override
	public double getUnpaidTaxes(String city) {
		return 0;
	}

	@Override
	public void setLastTaxes(String city, Date date) {

	}

	@Override
	public Date getLastTaxes(String city) {
		return null;
	}

	@Override
	public Map<String, Double> getUnpaidTaxes() {
		return new HashMap<>();
	}

	@Override
	public boolean hasDelegatedPermission(Player player, DelegatedPermission permission) {
		return true; // All admins have permissions over all admin properties
	}

	@Override
	public AdminLegalEntity findEntity(String tag) {
		return this;
	}

	@Override
	public String getTag(AdminLegalEntity entity) {
		return "";
	}

	@Override
	public String displayable() {
		return ChatColor.RED + "La Confédération";
	}

	@Override
	public String shortDisplayable() {
		return ChatColor.RED + "Confédération";
	}
}
