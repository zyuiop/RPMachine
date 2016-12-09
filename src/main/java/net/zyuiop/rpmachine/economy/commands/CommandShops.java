package net.zyuiop.rpmachine.economy.commands;

import net.md_5.bungee.api.ChatColor;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.economy.shops.ItemShopSign;
import net.zyuiop.rpmachine.economy.shops.ShopAction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author zyuiop
 */
public class CommandShops extends EconomixCommand {
	public CommandShops(RPMachine rpMachine) {
		super(rpMachine);
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		Player player = (Player) commandSender;
		for (ItemShopSign sign : rpMachine.getShopsManager().getPlayerShops(player)) {
			String typeLine = sign.getAction() == ShopAction.BUY ? ChatColor.RED + "Achat" : ChatColor.GREEN + "Vente";
			String size = (sign.getAvailable() > sign.getAmountPerPackage() ? ChatColor.GREEN : ChatColor.RED) + "" + sign.getAvailable() + " en stock";
			player.sendMessage(
					ChatColor.GOLD + " -> " +
							sign.getLocation().getBlockX() + "-" + sign.getLocation().getBlockY() + "-" + sign.getLocation().getBlockZ() +
							" : " + typeLine + ChatColor.YELLOW + " de " + sign.getItemType() + " : " + size);
		}
		return true;
	}
}
