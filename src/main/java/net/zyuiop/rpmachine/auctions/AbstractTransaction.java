package net.zyuiop.rpmachine.auctions;

import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.Ownable;
import net.zyuiop.rpmachine.json.JsonExclude;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * @author Louis Vialar
 */
public abstract class AbstractTransaction<T extends Order> implements Ownable {
    static int TX_ID = 0;
    static long TX_EXPIRATION_TIME = 20 * 1000L;

    private final int id;
    private final String owner;
    private final long startTime;
    private final TreeSet<T> content;
    private final int totalAmount;
    @JsonExclude
    boolean canceled = false;

    public AbstractTransaction(LegalEntity owner, int totalAmount) {
        this.id = TX_ID++;
        this.owner = owner.tag();
        this.startTime = System.currentTimeMillis();
        this.content = new TreeSet<>();
        this.totalAmount = totalAmount;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public int remainingSeconds() {
        long end = startTime + TX_EXPIRATION_TIME;
        long remain = end - System.currentTimeMillis();

        return (remain < 0 ? 0 : (int) (remain / 1000));
    }

    @Nullable
    @Override
    public String ownerTag() {
        return owner;
    }

    public abstract void cancel();

    boolean checkAutoCancel() {
        if (System.currentTimeMillis() > startTime + TX_EXPIRATION_TIME) {
            cancel();
        }

        return canceled;
    }

    public abstract boolean complete(Player player);

    protected int getAvailable(T order) {
        return order.getAvailable();
    }

    public double getPrice() {
        Iterator<T> auctionIterator = content.iterator();
        int remaining = totalAmount;
        double price = 0;
        while (remaining > 0) {
            T a = auctionIterator.next();

            if (remaining < getAvailable(a)) {
                price += remaining * a.getItemPrice();
            } else {
                price += a.getItemPrice() * getAvailable(a);
            }

            remaining -= getAvailable(a);
        }

        return price;
    }

    public TreeSet<T> getContent() {
        return content;
    }

    public int getId() {
        return id;
    }
}
