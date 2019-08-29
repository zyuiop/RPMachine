package net.zyuiop.rpmachine.auctions;

import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.json.JsonExclude;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * @author Louis Vialar
 */
public class SellTransaction extends AbstractTransaction<BuyOrder> {
    public SellTransaction(LegalEntity seller, int soldItems) {
        super(seller, soldItems);
    }

    public void cancel() {
        if (canceled)
            return;
        canceled = true;

        Iterator<BuyOrder> iter = getContent().iterator();

        while (iter.hasNext()) {
            BuyOrder next = iter.next();
            if (next != null)
                AuctionManager.INSTANCE.addAuction(next);
            iter.remove();
        }
        AuctionManager.INSTANCE.removeTransaction(this, false);
    }

    @Override
    protected int getAvailable(BuyOrder order) {
        return order.getRemainingItems();
    }

    public boolean complete(Player player) {
        checkAutoCancel();

        if (canceled) {
            player.sendMessage(ChatColor.RED + "Transaction déjà acceptée ou invalide.");
            AuctionManager.INSTANCE.removeTransaction(this);
            return false;
        }

        canceled = true;

        Iterator<BuyOrder> auctionIterator = getContent().iterator();
        int remaining = getTotalAmount();
        while (remaining > 0) {
            BuyOrder a = auctionIterator.next();

            if (remaining < a.getRemainingItems()) {
                BuyOrder toAdd = a.sellDirect(remaining, player, owner());

                AuctionManager.INSTANCE.addAuction(toAdd);
                AuctionManager.INSTANCE.removeTransaction(this);
                return true;
            } else {
                BuyOrder ret = a.sellDirect(a.getRemainingItems(), player, owner());

                if (ret == a)
                    return false; // Something happened

                AuctionManager.INSTANCE.addAuction(ret);
                remaining -= a.getRemainingItems();
            }

            auctionIterator.remove();
        }

        AuctionManager.INSTANCE.removeTransaction(this);
        return true;
    }
}
