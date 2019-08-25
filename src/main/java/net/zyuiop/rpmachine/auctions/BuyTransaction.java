package net.zyuiop.rpmachine.auctions;

import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Iterator;

/**
 * @author Louis Vialar
 */
public class BuyTransaction extends AbstractTransaction<SellOrder> {

    public BuyTransaction(LegalEntity buyer, int requestedItems) {
        super(buyer, requestedItems);
    }

    @Override
    public void cancel() {
        if (canceled)
            return;
        canceled = true;

        Iterator<SellOrder> iter = getContent().iterator();

        while (iter.hasNext()) {
            SellOrder next = iter.next();
            if (next != null)
                AuctionManager.INSTANCE.addAuction(next);
            iter.remove();
        }
        AuctionManager.INSTANCE.removeTransaction(this, false);
    }

    @Override
    public boolean complete(Player player) {
        // TXes are saved afterwards anyway

        checkAutoCancel();

        if (canceled) {
            player.sendMessage(ChatColor.RED + "Transaction déjà acceptée ou invalide.");
            AuctionManager.INSTANCE.removeTransaction(this, false);
            return false;
        }


        double price = getPrice();
        if (owner().getBalance() < price) {
            Messages.notEnoughMoneyEntity(player, owner(), price);
            cancel();
            return true;
        }

        canceled = true;

        Iterator<SellOrder> auctionIterator = getContent().iterator();
        int remaining = getTotalAmount();
        while (remaining > 0) {
            SellOrder a = auctionIterator.next();

            if (remaining < a.getAvailable()) {
                SellOrder toAdd = a.buy(remaining, player, owner());

                Bukkit.getLogger().info("Adding remainings of auction " + a.getItemPrice() + " " + a.getMaterial() + " " + a.ownerTag());

                AuctionManager.INSTANCE.addAuction(toAdd);
                AuctionManager.INSTANCE.removeTransaction(this, false);
                return true;
            } else {
                SellOrder ret = a.buy(a.getAvailable(), player, owner());

                if (ret == a)
                    return false; // Not enough money

                remaining -= a.getAvailable();
            }

            auctionIterator.remove();
        }

        AuctionManager.INSTANCE.removeTransaction(this, false);
        return true;
    }

}
