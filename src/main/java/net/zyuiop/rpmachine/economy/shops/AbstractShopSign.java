package net.zyuiop.rpmachine.economy.shops;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.economy.RoleToken;
import net.zyuiop.rpmachine.entities.Ownable;
import net.zyuiop.rpmachine.permissions.ShopPermissions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nullable;

public abstract class AbstractShopSign implements Ownable {
    protected VirtualLocation location;
    protected double price;
    protected String owner;

    public AbstractShopSign() {

    }

    @Nullable
    @Override
    public String ownerTag() {
        return owner;
    }

    public AbstractShopSign(Location location) {
        this.location = new VirtualLocation(location);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
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
        RoleToken token = RPMachine.getPlayerRoleToken(player);
        if (!owner.equals(token.getTag())) {
            clickUser(player, event);
        } else {
            clickPrivileged(player, token, event);
        }
    }

    protected abstract void doBreakSign(Player authorizedBreaker);

    public boolean breakSign(Player player) {
        RoleToken token = RPMachine.getPlayerRoleToken(player);
        if (owner.equals(token.getTag())) {
            if (!token.checkDelegatedPermission(ShopPermissions.DESTROY_SHOP))
                return false;

            doBreakSign(player);
            return true;
        }
        return false;
    }

    public abstract void breakSign();

    abstract void clickPrivileged(Player player, RoleToken token, PlayerInteractEvent event);

    abstract void clickUser(Player player, PlayerInteractEvent event);
}
