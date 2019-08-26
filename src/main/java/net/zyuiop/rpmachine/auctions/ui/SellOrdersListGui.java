package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.SellOrder;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.MenuItem;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Louis Vialar
 */
public class SellOrdersListGui extends AbstractOrderListGui<SellOrder> {
    SellOrdersListGui(Window backTo, Player player, Material mat) {
        super("Liste des ordres de vente", backTo, player, mat);
    }

    @Override
    protected Collection<SellOrder> getOrders() {
        return AuctionManager.INSTANCE.getAuctions(mat);
    }

    @Override
    protected MenuItem getItem(SellOrder a) {
        return new MenuItem(mat, Math.min(mat.getMaxStackSize(), a.getAvailable()))
                .setDescription(
                        ChatColor.YELLOW + "Quantité vendue : " + ChatColor.GOLD + a.getAvailable(),
                        ChatColor.YELLOW + "Prix unitaire : " + ChatColor.GOLD + String.format("%.2f", a.getItemPrice()) + RPMachine.getCurrencyName(),
                        ChatColor.YELLOW + "Vendeur : " + a.owner().shortDisplayable(),
                        ChatColor.GOLD + "",

                        ChatColor.YELLOW + "Prix du lot : " + ChatColor.GOLD + String.format("%.2f", (a.getAvailable() * a.getItemPrice())) + RPMachine.getCurrencyName(),
                        ChatColor.GOLD + "",
                        ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + " Clic : acheter le lot"
                );
    }

    @Override
    protected Runnable getAction(SellOrder a) {
        return () -> {
            if (AuctionManager.INSTANCE.removeAuction(a)) {
                close();

                new TransactionConfirmGui(player, mat, AuctionManager.INSTANCE.startBuyRemovedOrder(RPMachine.getPlayerActAs(player), a)).open();
            } else {
                player.sendMessage(ChatColor.RED + "Cette offre n'est plus disponible.");
                refresh();
            }
        };
    }
}
