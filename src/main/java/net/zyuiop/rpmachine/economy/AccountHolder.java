package net.zyuiop.rpmachine.economy;

/**
 * @author zyuiop
 */
public interface AccountHolder {
	double getMoney();

	void setMoney(double amount);

	boolean withdrawMoney(double amount);

	void creditMoney(double amount);
}
