package net.zyuiop.rpmachine.database;

import java.util.Date;
import java.util.Map;
import net.zyuiop.rpmachine.VirtualLocation;

/**
 * @author zyuiop
 */
public interface PlayerData {
	String getJob();

	void setJob(String job);

	VirtualLocation getHome();

	void setHome(VirtualLocation location);

	double getMoney();

	void setMoney(double amount);

	boolean withdrawMoney(double amount);

	void creditMoney(double amount);

	void setUnpaidTaxes(String city, double amount);

	double getUnpaidTaxes(String city);

	void setLastTaxes(String city, Date date);

	Date getLastTaxes(String city);

	Map<String, Double> getUnpaidTaxes();

	boolean isNew();
}
