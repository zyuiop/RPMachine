package net.zyuiop.rpmachine.database.bukkitbridge;

import com.google.gson.Gson;
import net.bridgesapi.api.BukkitBridge;
import net.zyuiop.rpmachine.database.ShopsManager;
import net.zyuiop.rpmachine.economy.shops.AbstractShopSign;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class BukkitBridgeShops extends ShopsManager {
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

	public void load() {
		Jedis jedis = BukkitBridge.get().getResource();
		Map<String, String> map = jedis.hgetAll("rpshops");
		jedis.close();

		for (Map.Entry<String, String> line : map.entrySet()) {
			Location loc = locFromString(line.getKey());

			AbstractShopSign sign = gson.fromJson(line.getValue(), AbstractShopSign.class);
			sign.display();
			Bukkit.getLogger().info("Loaded shop " + sign.getLocation().toString());
			signs.put(loc, sign);
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

	public void save(AbstractShopSign shopSign) {
		new Thread(() -> {
			Jedis jedis = BukkitBridge.get().getResource();
			jedis.hset("rpshops", locAsString(shopSign.getLocation()), new Gson().toJson(shopSign));
			jedis.close();
		}).start();
	}

}
