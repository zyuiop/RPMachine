package net.zyuiop.rpmachine.economy.shops;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.economy.ShopOwner;
import net.zyuiop.rpmachine.economy.TaxPayerToken;
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

	public void setLocation(VirtualLocation location) {
		this.location = location;
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
		if (owner.equals(token))
			clickOwner(player, event);
		else
			clickUser(player, event);
	}

	protected abstract void doBreakSign(Player authorizedBreaker);

	public boolean breakSign(Player player) {
		TaxPayerToken token = RPMachine.getPlayerRoleToken(player);
		if (owner.equals(token)) {
			doBreakSign(player);
			return true;
		}
		return false;
	}

	public abstract void breakSign();

	abstract void clickOwner(Player player, PlayerInteractEvent event);

	abstract void clickUser(Player player, PlayerInteractEvent event);
}
