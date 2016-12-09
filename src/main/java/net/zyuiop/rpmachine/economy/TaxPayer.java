package net.zyuiop.rpmachine.economy;

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
}
