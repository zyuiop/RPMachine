package net.zyuiop.rpmachine.auctions;

import com.google.common.base.Preconditions;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.Ownable;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * @author Louis Vialar
 */
public class Auction implements Ownable, Comparable<Auction> {
    private final Long id;
    private final Material material;
    private final double itemPrice;
    private final int available;
    private final String ownerTag;

    public Auction(Material material, double itemPrice, int available, String ownerTag) {
        Preconditions.checkNotNull(ownerTag);
        Preconditions.checkNotNull(material);
        Preconditions.checkArgument(itemPrice >= 0, "itemPrice cannot be negative");
        Preconditions.checkArgument(available >= 0, "available cannot be negative");

        this.id = System.currentTimeMillis();
        this.material = material;
        this.itemPrice = itemPrice;
        this.available = available;
        this.ownerTag = ownerTag;
    }

    public Material getMaterial() {
        return material;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public int getAvailable() {
        return available;
    }

    public Auction add(int amt) {
        if (amt < 0) throw new IllegalArgumentException("amt");

        return updateQty(available + amt);
    }

    private Auction updateQty(int newQty) {
        return new Auction(material, itemPrice, newQty, ownerTag);
    }

    public Auction remove(int amt) {
        if (amt < 0) throw new IllegalArgumentException("amt");

        return updateQty(available - amt);
    }

    public Auction buy(int amt, Player buyer, LegalEntity buyerEntity) {
        if (amt > available) {
            throw new IllegalArgumentException("amt");
        }

        double price = amt * getItemPrice();
        if (buyerEntity.transfer(price, owner())) {
            Messages.credit(owner(), price, "hotel des ventes - " + amt + " * " + material + " à " + itemPrice + RPMachine.getCurrencyName() + "/pièce par " + buyerEntity.shortDisplayable());
            Messages.debit(buyerEntity, price, "hotel des ventes - " + amt + " * " + material + " à " + itemPrice + RPMachine.getCurrencyName() + "/pièce à " + owner().shortDisplayable());
            buyer.getInventory().addItem(new ItemStack(material, amt));
            return remove(amt);
        } else {
            Messages.notEnoughMoneyEntity(buyer, buyerEntity, price);
            return this;
        }
    }

    public Auction updatePrice(double price) {
        return new Auction(material, price, available, ownerTag);
    }

    @Nullable
    @Override
    public String ownerTag() {
        return ownerTag;
    }

    @Override
    public int compareTo(Auction o) {
        int cmp = Double.compare(itemPrice, o.itemPrice);
        cmp = cmp == 0 ? Long.compare(id == null ? 0 : id, o.id == null ? 0 : o.id) : cmp;
        cmp = cmp == 0 ? ownerTag.compareTo(o.ownerTag) : cmp;

        return cmp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Auction)) return false;

        Auction auction = (Auction) o;

        if (Double.compare(auction.itemPrice, itemPrice) != 0) return false;
        if (available != auction.available) return false;
        if (id != null ? !id.equals(auction.id) : auction.id != null) return false;
        if (material != auction.material) return false;
        return ownerTag != null ? ownerTag.equals(auction.ownerTag) : auction.ownerTag == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id != null ? id.hashCode() : 0;
        result = 31 * result + (material != null ? material.hashCode() : 0);
        temp = Double.doubleToLongBits(itemPrice);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + available;
        result = 31 * result + (ownerTag != null ? ownerTag.hashCode() : 0);
        return result;
    }
}
