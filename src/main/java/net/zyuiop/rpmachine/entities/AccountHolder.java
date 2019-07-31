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

	default boolean transfer(double amount, AccountHolder target) {
		if (amount < 0)
			return false;

		if (canPay(amount)) {
			withdrawMoney(amount);
			target.creditMoney(amount);
			return true;
		}

		return false;
	}
}
