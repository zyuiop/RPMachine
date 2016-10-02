package net.zyuiop.rpmachine.database;

/**
 * @author zyuiop
 */
public interface FinancialCallback {
	void done(double newAmount, double diff);
}
