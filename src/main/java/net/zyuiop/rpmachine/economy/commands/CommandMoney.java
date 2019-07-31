package net.zyuiop.rpmachine.economy.commands;

import net.md_5.bungee.api.ChatColor;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.entities.LegalEntity;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMoney extends AbstractCommand {
	public CommandMoney() {
		super("money", null, "balance", "compte");
	}

	@Override
	protected boolean onPlayerCommand(Player player, String command, String[] args) {
		LegalEntity entity = RPMachine.getPlayerActAs(player);
		LegalEntity playerEntity = RPMachine.database().getPlayerData(player.getUniqueId());

		player.sendMessage(ChatColor.YELLOW + "Vous avez actuellement " + ChatColor.GOLD + playerEntity.getBalance() + EconomyManager.getMoneyName());
		if (entity != playerEntity) {
			player.sendMessage(entity.displayable() + ChatColor.YELLOW + " a actuellement " + ChatColor.GOLD + entity.getBalance() + EconomyManager.getMoneyName());
		}
		return false;
	}
}
