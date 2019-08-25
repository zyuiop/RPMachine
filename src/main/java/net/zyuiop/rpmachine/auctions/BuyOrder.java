package net.zyuiop.rpmachine.auctions;

import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

/**
 * @author Louis Vialar
 */
public class BuyOrder extends Order<BuyOrder> {
    private final int maxBuy;

    public BuyOrder(Material material, double itemPrice, String ownerTag, int maxBuy) {
        this(material, itemPrice, 0, ownerTag, maxBuy);
    }

    private BuyOrder(Material material, double itemPrice, int available, String ownerTag, int maxBuy) {
        super(material, itemPrice, available, ownerTag);
        this.maxBuy = maxBuy;
    }

    protected BuyOrder updateQty(int newQty) {
        return new BuyOrder(getMaterial(), getItemPrice(), newQty, ownerTag(), maxBuy);
    }

    public int getMaxBuy() {
        return maxBuy;
    }

    public int getRemainingItems() {
        return maxBuy - getAvailable();
    }

    public BuyOrder sellDirect(int amt, Player seller, LegalEntity sellerEntity) {
        if (amt + getAvailable() > maxBuy) {
            seller.sendMessage(ChatColor.RED + "Quantité maximale acceptée : " + (maxBuy - getAvailable()));
            return this;
        }

        double price = amt * getItemPrice();
        sellerEntity.creditMoney(price); // The amount is debited when the order is created :)
        Messages.credit(sellerEntity, price, "hotel des ventes - " + amt + " * " + getMaterial() + " à " + getFormattedItemPrice() + "/pièce à " + owner().shortDisplayable());
        Messages.sendMessage(owner(), sellerEntity.displayable() + ChatColor.GRAY + " vient de vous vendre " + ChatColor.YELLOW + getMaterial() + " * " + amt + ChatColor.GRAY + " à l'HdV. Vous achetez encore " + ChatColor.YELLOW + (maxBuy - getAvailable() - amt) + " items.");
        return add(amt);
    }

    @Override
    public int compareTo(BuyOrder o) {
        int cmp = Double.compare(o.getItemPrice(), getItemPrice()); // reversed : highest price comes first !
        cmp = cmp == 0 ? Long.compare(o.getId() == null ? 0 : o.getId(), getId() == null ? 0 : getId()) : cmp;
        cmp = cmp == 0 ? o.ownerTag().compareTo(ownerTag()) : cmp;

        return cmp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BuyOrder)) return false;

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
