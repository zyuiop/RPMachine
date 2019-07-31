package net.zyuiop.rpmachine.shops;

import net.md_5.bungee.api.ChatColor;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.shops.ItemShopSign;
import net.zyuiop.rpmachine.shops.ShopAction;
import org.bukkit.entity.Player;

/**
 * @author zyuiop
 */
public class CommandShops extends AbstractCommand {
	public CommandShops() {
		super("shops", null, "boutiques", "myshops");
	}

	@Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        for (ItemShopSign sign : RPMachine.getInstance().getShopsManager().getPlayerShops(player)) {
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
