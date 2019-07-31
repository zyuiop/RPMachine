package net.zyuiop.rpmachine.economy;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.FinancialCallback;
import net.zyuiop.rpmachine.database.PlayerData;

import java.util.UUID;
import java.util.function.Consumer;

public class EconomyManager {
	private static String moneyName = null;
	private static double baseAmount = -1;

	private PlayerData getData(UUID player) {
		return RPMachine.database().getPlayerData(player);
	}

	@Deprecated
	public double getAmount(UUID player) {
		PlayerData data = getData(player);
		return data.getMoney();
	}

	@Deprecated
	public void giveMoney(UUID player, double amount) {
		giveMoney(player, amount, null);
	}

	@Deprecated
	public void giveMoney(UUID player, double amount, FinancialCallback callback) {
		new Thread(() -> {
			PlayerData data = getData(player);
			data.creditMoney(amount);
			if (callback != null)
				callback.done(data.getMoney(), true);
		}).start();
	}

	@Deprecated
	public void withdrawMoney(UUID player, double amount) {
		withdrawMoney(player, amount, null);
	}

	@Deprecated
	public void withdrawMoney(UUID player, double amount, FinancialCallback callback) {
		giveMoney(player, -amount, callback); // Same thing with a negative amount
	}

	@Deprecated
	public void withdrawMoneyWithBalanceCheck(UUID player, double amount, FinancialCallback callback) {
		withdrawMoneyWithBalanceCheck(getData(player), amount, callback);
	}

	public void withdrawMoneyWithBalanceCheck(TaxPayer payer, double amount, FinancialCallback callback) {
		new Thread(() -> {
			if (payer.withdrawMoney(amount)) {
				if (callback != null)
					callback.done(payer.getMoney(), true);
			} else {
				if (callback != null)
					callback.done(payer.getMoney(), false);
			}
		}).start();
	}

	@Deprecated
	public void transferMoney(UUID from, UUID to, double amount) {
		new Thread(() -> {
			PlayerData fromData = getData(from);
			PlayerData toData = getData(to);

			fromData.creditMoney(-amount);
			toData.creditMoney(amount);
		}).start();
	}

	@Deprecated
	public void transferMoneyBalanceCheck(UUID from, UUID to, double amount, Consumer<Boolean> result) {
		PlayerData fromData = getData(from);
		PlayerData toData = getData(to);

		transferMoneyBalanceCheck(fromData, toData, amount, result);
	}

	public void transferMoneyBalanceCheck(AccountHolder fromData, AccountHolder toData, double amount, Consumer<Boolean> result) {
		new Thread(() -> {
			if (fromData.withdrawMoney(amount)) {
				toData.creditMoney(amount);

				if (result != null)
					result.accept(true);
			} else if (result != null)
				result.accept(false);
		}).start();
	}

	public static String getMoneyName() {
		if (moneyName == null) {
			moneyName = RPMachine.getInstance().getConfig().getString("money.symbol", "$");
		}
		return moneyName;
	}

	public static double getBaseAmount() {
		if (baseAmount == -1) {
			baseAmount = RPMachine.getInstance().getConfig().getDouble("money.baseAmount", 150D);
		}
		return baseAmount;
	}
}
