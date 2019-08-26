package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.BuyOrder;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import net.zyuiop.rpmachine.utils.MenuItem;
import net.zyuiop.rpmachine.utils.Messages;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

/**
 * @author Louis Vialar
 */
class MyBuyOrdersGui extends AbstractOrderListGui<BuyOrder> {
    MyBuyOrdersGui(Window backTo, Player player, Material mat) {
        super("Mes offres d'achat", backTo, player, mat);
    }

    @Override
    protected Collection<BuyOrder> getOrders() {
        return AuctionManager.INSTANCE.getMyBuys(mat, RPMachine.getPlayerActAs(player));
    }

    @Override
    protected MenuItem getItem(BuyOrder a) {
        double refund = a.getItemPrice() * a.getRemainingItems();

        return new MenuItem(mat, Math.max(1, Math.min(mat.getMaxStackSize(), a.getAvailable())))
                .setDescription(
                        ChatColor.YELLOW + "Quantité disponible : " + ChatColor.GOLD + a.getAvailable(),
                        ChatColor.YELLOW + "Quantité totale à acheter : " + ChatColor.GOLD + a.getMaxBuy(),
                        ChatColor.YELLOW + "Quantité restant à acheter : " + ChatColor.GOLD + a.getRemainingItems(),
                        ChatColor.YELLOW + " ",
                        ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + " Clic : récupérer le lot",
                        ChatColor.GRAY + "Votre offre sera retirée, vous obtiendrez vos items et " + String.format("%.4f", refund) + RPMachine.getCurrencyName()
                );
    }

    @Override
    protected Runnable getAction(BuyOrder a) {
        return () -> {
            int availableSize = InventoryUtils.availablePlaceFor(player.getInventory(), mat);

            if (availableSize < a.getAvailable()) {
                player.sendMessage(ChatColor.RED + "Pas assez de place ! Maximum dispo : " + availableSize + " items.");
                refresh();
                return;
            }

            if (AuctionManager.INSTANCE.removeAuction(a)) {
                InventoryUtils.giveItems(mat, a.getAvailable(), player.getInventory());
                double actualRefund = a.getItemPrice() * a.getRemainingItems();
                RPMachine.getPlayerActAs(player).creditMoney(actualRefund);
                Messages.credit(RPMachine.getPlayerActAs(player), actualRefund, "retrait d'une offre d'achat");
                player.sendMessage(ChatColor.GREEN + "Offre récupérée !");
                refresh();
            } else {
                player.sendMessage(ChatColor.RED + "Cette offre n'est plus disponible.");
                refresh();
            }

        };
    }
}
