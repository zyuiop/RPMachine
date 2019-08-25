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
public abstract class Order<T extends Order> implements Ownable, Comparable<T> {
    private final Long id;
    private final Material material;
    private final double itemPrice;
    private final int available;
    private final String ownerTag;

    protected Order(Material material, double itemPrice, int available, String ownerTag) {
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

    public String getFormattedItemPrice() {
        return String.format("%.2f", getItemPrice()) + RPMachine.getCurrencyName();
    }

    public int getAvailable() {
        return available;
    }

    public T add(int amt) {
        if (amt < 0) throw new IllegalArgumentException("amt");

        return updateQty(available + amt);
    }

    public Long getId() {
        return id;
    }

    protected abstract T updateQty(int newQty);

    public T remove(int amt) {
        if (amt < 0) throw new IllegalArgumentException("amt");

        return updateQty(available - amt);
    }

    @Nullable
    @Override
    public String ownerTag() {
        return ownerTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;

        Order<?> order = (Order<?>) o;

        if (Double.compare(order.itemPrice, itemPrice) != 0) return false;
        if (available != order.available) return false;
        if (id != null ? !id.equals(order.id) : order.id != null) return false;
        if (material != order.material) return false;
        return ownerTag != null ? ownerTag.equals(order.ownerTag) : order.ownerTag == null;
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
