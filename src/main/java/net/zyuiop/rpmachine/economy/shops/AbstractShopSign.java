package net.zyuiop.rpmachine.economy.shops;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.economy.TaxPayerToken;
import net.zyuiop.rpmachine.permissions.ShopPermissions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class AbstractShopSign {
    protected VirtualLocation location;
    protected double price;
    protected TaxPayerToken owner;

    public AbstractShopSign() {

    }

    public AbstractShopSign(Location location) {
        this.location = new VirtualLocation(location);
    }

    public TaxPayerToken getOwner() {
        return owner;
    }

    public void setOwner(TaxPayerToken owner) {
        this.owner = owner;
    }

    public Location getLocation() {
        return location.getLocation();
    }

    public void setLocation(VirtualLocation location) {
        this.location = location;
    }

    public void setLocation(Location location) {
        this.location = new VirtualLocation(location);
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public abstract void display();

    public void rightClick(Player player, PlayerInteractEvent event) {
        TaxPayerToken token = RPMachine.getPlayerRoleToken(player);
        if (!owner.equals(token)) {
            clickUser(player, event);
        } else {
            clickPrivileged(player, token, event);
        }
    }

    protected abstract void doBreakSign(Player authorizedBreaker);

    public boolean breakSign(Player player) {
        TaxPayerToken token = RPMachine.getPlayerRoleToken(player);
        if (owner.equals(token)) {
            if (!token.checkDelegatedPermission(player, ShopPermissions.DESTROY_SHOP))
                return false;

            doBreakSign(player);
            return true;
        }
        return false;
    }

    public abstract void breakSign();

    abstract void clickPrivileged(Player player, TaxPayerToken token, PlayerInteractEvent event);

    abstract void clickUser(Player player, PlayerInteractEvent event);
}
