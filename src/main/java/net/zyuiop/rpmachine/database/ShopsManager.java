package net.zyuiop.rpmachine.database;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.zyuiop.rpmachine.economy.shops.AbstractShopSign;
import net.zyuiop.rpmachine.economy.shops.ShopSign;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public abstract class ShopsManager {
	protected final ConcurrentHashMap<Location, AbstractShopSign> signs = new ConcurrentHashMap<>();

	public ShopsManager() {
		load();
	}

	public final void create(AbstractShopSign sign) {
		signs.put(sign.getLocation(), sign);
		sign.display();
		doCreate(sign);
	}

	protected abstract void doCreate(AbstractShopSign shopSign);

	protected abstract void load();

	protected String locAsString(Location loc) {
		return loc.getWorld().getName() + "-" + loc.getBlockX() + "-" + loc.getBlockY() + "-" + loc.getBlockZ();
	}

	protected Location locFromString(String loc) {
		String[] parts = loc.split("-");
		return new Location(Bukkit.getWorld(parts[0]), Integer.valueOf(parts[1]), Integer.valueOf(parts[2]), Integer.valueOf(parts[3]));
	}

	public final void remove(AbstractShopSign shopSign) {
		doRemove(shopSign);
		signs.remove(shopSign.getLocation());
	}

	protected abstract void doRemove(AbstractShopSign shopSign);

	public final AbstractShopSign get(Location location) {
		return signs.get(location);
	}

	public abstract void save(AbstractShopSign shopSign);

	public final HashSet<ShopSign> getPlayerShops(UUID player) {
		HashSet<ShopSign> ret = new HashSet<>();
		for (AbstractShopSign shopSign : signs.values())
			if (shopSign.getOwnerId().equals(player) && shopSign instanceof ShopSign)
				ret.add((ShopSign) shopSign);

		return ret;
	}

}
