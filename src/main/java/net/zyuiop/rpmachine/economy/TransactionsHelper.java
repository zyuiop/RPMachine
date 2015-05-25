package net.zyuiop.rpmachine.economy;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class TransactionsHelper {

	private final EconomyManager manager;

	public TransactionsHelper(EconomyManager manager) {
		this.manager = manager;
	}

	public void transaction(Player from, Player to, double amount) {
		new Thread(() -> {
			if (manager.canPay(from.getUniqueId(), amount)) {
				manager.transferMoney(from.getUniqueId(), to.getUniqueId(), amount);
				from.sendMessage(Messages.SENT_MONEY.getMessage().replace("{AMT}", "" + amount));
				to.sendMessage(Messages.RECEIVED_MONEY.getMessage().replace("{AMT}", "" + amount).replace("{FROM}", from.getName()));
			} else {
				from.sendMessage(Messages.NOT_ENOUGH_MONEY.getMessage());
			}
		}).start();
	}

	public void transaction(Player from, UUID to, double amount) {
		new Thread(() -> {
			if (manager.canPay(from.getUniqueId(), amount)) {
				manager.transferMoney(from.getUniqueId(), to, amount);
				from.sendMessage(Messages.SENT_MONEY.getMessage().replace("{AMT}", "" + amount));
			} else {
				from.sendMessage(Messages.NOT_ENOUGH_MONEY.getMessage());
			}
		}).start();
	}

	public void displayAmount(Player player) {
		new Thread(() -> {
			double amt = manager.getAmount(player.getUniqueId());
			player.sendMessage(Messages.AMOUNT_MESSAGE.getMessage().replace("{AMT}", "" + amt));
		}).start();
	}

}
