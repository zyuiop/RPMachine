package net.zyuiop.rpmachine.economy;

import net.zyuiop.rpmachine.cities.LandOwner;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zyuiop
 */
public class AdminAccountHolder implements TaxPayer, LandOwner, ShopOwner {
	public static final AdminAccountHolder INSTANCE = new AdminAccountHolder();

	private AdminAccountHolder() {

	}

	@Override
	public double getMoney() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void setMoney(double amount) {

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
	public boolean canManagePlot(Player player) {
		return player.hasPermission("plots.manageAdminPlots");
	}

	@Override
	public boolean canManageShop(Player player) {
		return player.hasPermission("shops.manageAdminShops");
	}
}
