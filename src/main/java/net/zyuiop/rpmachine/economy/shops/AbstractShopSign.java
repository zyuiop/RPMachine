package net.zyuiop.rpmachine.economy.shops;

import net.zyuiop.rpmachine.VirtualLocation;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public abstract class AbstractShopSign {

	protected VirtualLocation location;
	protected double price;
	protected UUID ownerId;
	protected String className;

	public AbstractShopSign() {

	}

	public AbstractShopSign(Class<? extends AbstractShopSign> type) {
		className = type.getName();
	}

	public AbstractShopSign(Class<? extends AbstractShopSign> type, Location location) {
		this(type);
		setLocation(location);
	}

	public String getClassName() {
		return className;
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	public void setLocation(VirtualLocation location) {
		this.location = location;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public AbstractShopSign(Location location) {
		this.location = new VirtualLocation(location);
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

	public abstract void rightClick(Player player, PlayerInteractEvent event);

	public abstract boolean breakSign(Player player);

	abstract void clickOwner(Player player, PlayerInteractEvent event);

	abstract void clickUser(Player player, PlayerInteractEvent event);


}
