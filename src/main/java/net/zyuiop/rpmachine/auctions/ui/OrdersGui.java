package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.BuyOrder;
import net.zyuiop.rpmachine.auctions.Order;
import net.zyuiop.rpmachine.auctions.SellOrder;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import net.zyuiop.rpmachine.utils.MenuItem;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
public class OrdersGui extends AbstractOrderListGui<Order> {
    OrdersGui(Window backTo, Player player, Material mat) {
        super("Liste des ordres", backTo, player, mat);
    }

    @Override
    protected Collection<Order> getOrders() {
        List<Order> orders = new ArrayList<>();
        orders.addAll(AuctionManager.INSTANCE.getSellOrders(mat));
        orders.addAll(AuctionManager.INSTANCE.getBuyOrders(mat).stream().filter(a -> a.getRemainingItems() > 0).collect(Collectors.toList()));
        return orders;
    }

    @Override
    protected MenuItem getItem(Order order) {
        if (order instanceof SellOrder) return getItem((SellOrder) order);
        else if (order instanceof BuyOrder) return getItem((BuyOrder) order);
        else return null;
    }

    @Override
    protected Runnable getAction(Order order) {
        if (order instanceof SellOrder) return getAction((SellOrder) order);
        else if (order instanceof BuyOrder) return getAction((BuyOrder) order);
        else return null;
    }

    protected MenuItem getItem(SellOrder a) {
        return new MenuItem(mat, Math.min(mat.getMaxStackSize(), a.getAvailable()))
                .setDescription(
                        ChatColor.YELLOW + "VENTE",
                        ChatColor.YELLOW + " ",
                        ChatColor.YELLOW + "Quantité vendue : " + ChatColor.GOLD + a.getAvailable(),
                        ChatColor.YELLOW + "Prix unitaire : " + ChatColor.GOLD + String.format("%.2f", a.getItemPrice()) + RPMachine.getCurrencyName(),
                        ChatColor.YELLOW + "Vendeur : " + a.owner().shortDisplayable(),
                        ChatColor.GOLD + "",

                        ChatColor.YELLOW + "Prix du lot : " + ChatColor.GOLD + String.format("%.2f", (a.getAvailable() * a.getItemPrice())) + RPMachine.getCurrencyName(),
                        ChatColor.GOLD + "",
                        ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + " Clic : acheter le lot"
                )
                .glowing();
    }

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

    protected MenuItem getItem(BuyOrder a) {
        return new MenuItem(mat, Math.min(mat.getMaxStackSize(), a.getRemainingItems()))
                .setDescription(
                        ChatColor.GREEN + "ACHAT",
                        ChatColor.YELLOW + " ",
                        ChatColor.YELLOW + "Quantité achetée : " + ChatColor.GOLD + a.getRemainingItems(),
                        ChatColor.YELLOW + "Prix unitaire : " + ChatColor.GOLD + String.format("%.2f", a.getItemPrice()) + RPMachine.getCurrencyName(),
                        ChatColor.YELLOW + "Acheteur : " + a.owner().shortDisplayable(),
                        ChatColor.GOLD + "",

                        ChatColor.YELLOW + "Prix du lot : " + ChatColor.GOLD + String.format("%.2f", (a.getRemainingItems() * a.getItemPrice())) + RPMachine.getCurrencyName(),
                        ChatColor.GOLD + "",
                        ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + " Clic : vendre le lot"
                );
    }

    protected Runnable getAction(BuyOrder a) {
        return () -> {
            Iterator<ItemStack> iterator = player.getInventory().iterator();
            int toAdd = a.getRemainingItems();

            while (iterator.hasNext() && toAdd > 0) {
                ItemStack next = iterator.next();
                if (next != null && next.getType() == mat) {
                    if (toAdd >= next.getAmount()) {
                        toAdd -= next.getAmount();
                        iterator.remove();
                    } else {
                        next.setAmount(next.getAmount() - toAdd);
                        toAdd = 0;
                    }
                }
            }

            if (toAdd > 0) {
                int added = a.getRemainingItems() - toAdd;
                player.sendMessage(ChatColor.RED + "Vous n'avez pas les items requis dans votre inventaire.");
                InventoryUtils.giveItems(mat, added, player.getInventory());
                return;
            }

            if (AuctionManager.INSTANCE.removeAuction(a)) {
                close();

                new TransactionConfirmGui(player, mat, AuctionManager.INSTANCE.startSellRemovedOrder(RPMachine.getPlayerActAs(player), a)).open();
            } else {
                InventoryUtils.giveItems(mat, a.getRemainingItems(), player.getInventory());

                player.sendMessage(ChatColor.RED + "Cette offre n'est plus disponible.");
                refresh();
            }
        };
    }
}
