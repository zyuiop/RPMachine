package net.zyuiop.rpmachine.database.bukkitbridge;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import net.bridgesapi.api.BukkitBridge;
import net.zyuiop.rpmachine.database.ShopsManager;
import net.zyuiop.rpmachine.economy.shops.AbstractShopSign;
import net.zyuiop.rpmachine.economy.shops.ShopGsonHelper;
import net.zyuiop.rpmachine.economy.shops.ShopSign;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import redis.clients.jedis.Jedis;

public class BukkitBridgeShops extends ShopsManager {
	private final ConcurrentHashMap<Location, AbstractShopSign> signs = new ConcurrentHashMap<>();

	public BukkitBridgeShops() {
		load();
	}

	@Override
	protected void doCreate(AbstractShopSign sign) {
		new Thread(() -> {
			Jedis jedis = BukkitBridge.get().getResource();
			jedis.hset("rpshops", locAsString(sign.getLocation()), new Gson().toJson(sign));
			jedis.close();
		}).start();
	}

	public void load()  {
			Jedis jedis = BukkitBridge.get().getResource();
			Map<String, String> map = jedis.hgetAll("rpshops");
			jedis.close();

			Gson gson = new Gson();
			for (Map.Entry<String, String> line : map.entrySet()) {
				Location loc = locFromString(line.getKey());
				ShopGsonHelper asign = gson.fromJson(line.getValue(), ShopGsonHelper.class);
				try {
					Class<? extends AbstractShopSign> clazz = (Class<? extends AbstractShopSign>) Class.forName(asign.getClassName());
					AbstractShopSign sign = gson.fromJson(line.getValue(), clazz);
					sign.display();
					Bukkit.getLogger().info("Loaded shop " + sign.getLocation().toString());
					signs.put(loc, sign);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
	}

	@Override
	protected void doRemove(AbstractShopSign shopSign) {
		new Thread(() -> {
			Jedis jedis = BukkitBridge.get().getResource();
			jedis.hdel("rpshops", locAsString(shopSign.getLocation()));
			jedis.close();
		}).start();
	}

	public AbstractShopSign get(Location location) {
		return signs.get(location);
	}

	public void save(AbstractShopSign shopSign) {
		new Thread(() -> {
			Jedis jedis = BukkitBridge.get().getResource();
			jedis.hset("rpshops", locAsString(shopSign.getLocation()), new Gson().toJson(shopSign));
			jedis.close();
		}).start();
	}

	public HashSet<ShopSign> getPlayerShops(UUID player) {
		HashSet<ShopSign> ret = new HashSet<>();
		for (AbstractShopSign shopSign : signs.values())
			if (shopSign.getOwnerId().equals(player) && shopSign instanceof ShopSign)
				ret.add((ShopSign) shopSign);

		return ret;
	}

}
