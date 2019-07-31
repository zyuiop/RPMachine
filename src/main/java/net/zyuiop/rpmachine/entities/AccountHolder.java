package net.zyuiop.rpmachine.entities;

/**
 * Represents anything that can have money
 * @author zyuiop
 */
public interface AccountHolder {
	/**
	 * Get the current balance of the account
	 * @return
	 */
	double getBalance();

	void setBalance(double amount);

	boolean withdrawMoney(double amount);

	void creditMoney(double amount);

	default boolean canPay(double amount) {
		return getBalance() > amount;
	}
}
