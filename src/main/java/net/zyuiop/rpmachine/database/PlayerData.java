package net.zyuiop.rpmachine.database;

import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.economy.AccountHolder;
import net.zyuiop.rpmachine.economy.TaxPayer;

/**
 * @author zyuiop
 */
public interface PlayerData extends AccountHolder, TaxPayer {
	String getJob();

	void setJob(String job);

	VirtualLocation getHome();

	void setHome(VirtualLocation location);

	boolean isNew();

	boolean togglePlotMessages();
}
