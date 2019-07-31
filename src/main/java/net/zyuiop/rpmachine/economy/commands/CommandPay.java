package net.zyuiop.rpmachine.economy.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.economy.RoleToken;
import net.zyuiop.rpmachine.permissions.EconomyPermissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CommandPay extends AbstractCommand {
	public CommandPay() {
		super("pay", null, "payer", "send");
	}

	@Override
	protected boolean onPlayerCommand(Player player, String command, String[] args) {
		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "Utilisation : /pay <joueur à payer> <montant à payer>");
			return true;
		}

		RoleToken transactionFrom = RPMachine.getPlayerRoleToken(player);
		Player target = Bukkit.getPlayerExact(args[0]);
		if (!transactionFrom.hasDelegatedPermission(EconomyPermissions.PAY_MONEY_TO_PLAYER)) {
			player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de faire ça en tant que " + transactionFrom.getLegalEntity().displayable());
		} else if (target == null) {
			player.sendMessage(ChatColor.RED + "Le joueur est actuellement hors ligne.");
			return true;
		} else if (target.getUniqueId().equals(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous donner d'argent à vous même.");
			return true;
		} else {
			new Thread(() -> {
				Double val = Double.valueOf(args[1]);
				if (val < 0)
					val = -val;

				RPMachine.getInstance().getTransactionsHelper().transaction(player, transactionFrom, target, val);
			}).start();
		}

		return true;
	}
}
