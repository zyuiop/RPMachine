package net.zyuiop.rpmachine.economy;

import net.zyuiop.rpmachine.entities.RoleToken;
import org.bukkit.entity.Player;

public class TransactionsHelper {

	private final EconomyManager manager;

	public TransactionsHelper(EconomyManager manager) {
		this.manager = manager;
	}

	public void transaction(Player from, RoleToken fromToken, Player to, double amount) {
		new Thread(() -> {
			if (fromToken.getLegalEntity().withdrawMoney(amount)) {
				manager.giveMoney(to.getUniqueId(), amount);
				from.sendMessage(Messages.SENT_MONEY.getMessage().replace("{AMT}", "" + amount));
				to.sendMessage(Messages.RECEIVED_MONEY.getMessage().replace("{AMT}", "" + amount).replace("{FROM}", fromToken.getLegalEntity().displayable()));
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
