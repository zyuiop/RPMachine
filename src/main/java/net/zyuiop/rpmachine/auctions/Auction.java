package net.zyuiop.rpmachine.auctions;

import com.google.common.base.Preconditions;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.Ownable;
import net.zyuiop.rpmachine.permissions.EconomyPermissions;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * @author Louis Vialar
 */
public class Auction implements Ownable, Comparable<Auction> {
    private final Material material;
    private final double itemPrice;
    private final int available;
    private final String ownerTag;

    public Auction(Material material, double itemPrice, int available, String ownerTag) {
        Preconditions.checkNotNull(ownerTag);
        Preconditions.checkNotNull(material);
        Preconditions.checkArgument(itemPrice >= 0, "itemPrice cannot be negative");
        Preconditions.checkArgument(available >= 0, "available cannot be negative");

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
        return Double.compare(itemPrice, o.itemPrice);
    }
}
