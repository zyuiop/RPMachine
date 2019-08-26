package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.Order;
import net.zyuiop.rpmachine.auctions.SellOrder;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import net.zyuiop.rpmachine.utils.MenuItem;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * @author Louis Vialar
 */
class MySellOrdersGui extends AbstractOrderListGui<SellOrder> {
    MySellOrdersGui(Window backTo, Player player, Material mat) {
        super("Mes ordres de vente", backTo, player, mat);
    }

    @Override
    protected Collection<SellOrder> getOrders() {
        return AuctionManager.INSTANCE.getMyAuctions(mat, RPMachine.getPlayerActAs(player));
    }

    @Override
    protected MenuItem getItem(SellOrder a) {
        return new MenuItem(mat, Math.min(mat.getMaxStackSize(), a.getAvailable()))
                .setDescription(
                        ChatColor.YELLOW + "Unités restantes : " + ChatColor.GOLD + a.getAvailable(),
                        ChatColor.YELLOW + "Prix unitaire : " + ChatColor.GOLD + String.format("%.2f", a.getItemPrice()) + RPMachine.getCurrencyName(),
                        ChatColor.GOLD + "",

                        ChatColor.YELLOW + "Prix min actuel : " + ChatColor.GOLD + String.format("%.2f", AuctionManager.INSTANCE.minPrice(mat)) + RPMachine.getCurrencyName(),
                        ChatColor.GOLD + "",
                        ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + " Clic : récupérer le lot"
                );
    }

    @Override
    protected Runnable getAction(SellOrder a) {
        return () -> {
            int availableSize = InventoryUtils.availablePlaceFor(player.getInventory(), mat);

            if (availableSize < a.getAvailable()) {
                player.sendMessage(ChatColor.RED + "Pas assez de place ! Maximum dispo : " + availableSize + " items.");
                refresh();
                return;
            }

            if (AuctionManager.INSTANCE.removeAuction(a)) {
                InventoryUtils.giveItems(mat, a.getAvailable(), player.getInventory());
                player.sendMessage(ChatColor.GREEN + "Offre récupérée !");
                refresh();
            } else {
                player.sendMessage(ChatColor.RED + "Cette offre n'est plus disponible.");
                refresh();
            }

        };
    }
}
