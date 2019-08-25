package net.zyuiop.rpmachine.auctions;

import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Louis Vialar
 */
public class SellOrder extends Order<SellOrder> {
    public SellOrder(Material material, double itemPrice, int available, String ownerTag) {
        super(material, itemPrice, available, ownerTag);
    }

    protected SellOrder updateQty(int newQty) {
        return new SellOrder(getMaterial(), getItemPrice(), newQty, ownerTag());
    }

    public SellOrder buy(int amt, Player buyer, LegalEntity buyerEntity) {
        if (amt > getAvailable()) {
            throw new IllegalArgumentException("amt");
        }

        double price = amt * getItemPrice();
        if (buyerEntity.transfer(price, owner())) {
            Messages.credit(owner(), price, "hotel des ventes - " + amt + " * " + getMaterial() + " à " + getFormattedItemPrice() + "/pièce par " + buyerEntity.shortDisplayable());
            Messages.debit(buyerEntity, price, "hotel des ventes - " + amt + " * " + getMaterial() + " à " + getFormattedItemPrice() + "/pièce à " + owner().shortDisplayable());
            buyer.getInventory().addItem(new ItemStack(getMaterial(), amt));
            return remove(amt);
        } else {
            Messages.notEnoughMoneyEntity(buyer, buyerEntity, price);
            return this;
        }
    }

    @Override
    public int compareTo(SellOrder o) {
        int cmp = Double.compare(getItemPrice(), o.getItemPrice());
        cmp = cmp == 0 ? Long.compare(getId() == null ? 0 : getId(), o.getId() == null ? 0 : o.getId()) : cmp;
        cmp = cmp == 0 ? ownerTag().compareTo(o.ownerTag()) : cmp;

        return cmp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SellOrder)) return false;

        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
