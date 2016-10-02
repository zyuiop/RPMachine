package net.zyuiop.rpmachine.economy;

import net.bridgesapi.api.BukkitBridge;
import net.bridgesapi.api.player.FinancialCallback;
import net.bridgesapi.api.player.PlayerData;

import java.util.UUID;
import java.util.function.Consumer;

public class EconomyManager {

	private PlayerData getData(UUID player) {
		return BukkitBridge.get().getPlayerManager().getPlayerData(player);
	}

	public boolean canPay(UUID player, double amount) {
		return getAmount(player) >= amount;
	}

	public double getAmount(UUID player) {
		PlayerData data = getData(player);
		return data.getDouble("rpmoney", 0D);
	}

	public void giveMoney(UUID player, double amount) {
		giveMoney(player, amount, null);
	}

	public void giveMoney(UUID player, double amount, FinancialCallback<Double> callback) {
		new Thread(() -> {
			PlayerData data = getData(player);
			data.setDouble("rpmoney", data.getDouble("rpmoney", 0D) + amount);
			if (callback != null)
				callback.done(data.getDouble("rpmoney", 0D), amount, null);
		}).start();
	}

	public void withdrawMoney(UUID player, double amount) {
		withdrawMoney(player, amount, null);
	}

	public void withdrawMoney(UUID player, double amount, FinancialCallback<Double> callback) {
		new Thread(() -> {
			PlayerData data = getData(player);
			data.setDouble("rpmoney", data.getDouble("rpmoney", 0D) - amount);
			if (callback != null)
				callback.done(data.getDouble("rpmoney", 0D), -amount, null);
		}).start();
	}

	public void withdrawMoneyWithBalanceCheck(UUID player, double amount, FinancialCallback<Double> callback) {
		new Thread(() -> {
			PlayerData data = getData(player);
			if (data.getDouble("rpmoney", 0D) >= amount) {
				data.setDouble("rpmoney", data.getDouble("rpmoney", 0D) - amount);
				if (callback != null)
					callback.done(data.getDouble("rpmoney", 0D), - amount, null);
			} else if (callback != null) {
				callback.done(data.getDouble("rpmoney", 0D), 0D, null);
			}
		}).start();
	}

	public void transferMoney(UUID from, UUID to, double amount) {
		new Thread(() -> {
			PlayerData fromData = getData(from);
			PlayerData toData = getData(to);

			fromData.setDouble("rpmoney", fromData.getDouble("rpmoney", 0D) - amount);
			toData.setDouble("rpmoney", toData.getDouble("rpmoney", 0D) + amount);
		}).start();
	}

	public void transferMoneyBalanceCheck(UUID from, UUID to, double amount, Consumer<Boolean> result) {
		new Thread(() -> {
			PlayerData fromData = getData(from);
			PlayerData toData = getData(to);

			if (fromData.getDouble("rpmoney", 0D) < amount) {
				if (result != null)
					result.accept(false);
				return;
			}

			fromData.setDouble("rpmoney", fromData.getDouble("rpmoney", 0D) - amount);
			toData.setDouble("rpmoney", toData.getDouble("rpmoney", 0D) + amount);

			if (result != null)
				result.accept(true);
		}).start();
	}

}
