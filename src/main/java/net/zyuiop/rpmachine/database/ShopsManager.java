package net.zyuiop.rpmachine.database;

import com.google.gson.*;
import net.zyuiop.rpmachine.economy.shops.AbstractShopSign;
import net.zyuiop.rpmachine.economy.shops.ShopSign;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ShopsManager implements JsonDeserializer<AbstractShopSign>, JsonSerializer<AbstractShopSign> {
	protected final ConcurrentHashMap<Location, AbstractShopSign> signs = new ConcurrentHashMap<>();
	protected final Gson gson = new GsonBuilder().registerTypeAdapter(AbstractShopSign.class, this).create();

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

	@Override
	public AbstractShopSign deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
		String clazz = element.getAsJsonObject().get("signClass").getAsString();
		try {
			return context.deserialize(element, Class.forName(clazz));
		} catch (ClassNotFoundException e) {
			throw new JsonParseException(e);
		}
	}

	@Override
	public JsonElement serialize(AbstractShopSign sign, Type type, JsonSerializationContext context) {
		JsonObject ser = context.serialize(sign).getAsJsonObject();
		ser.addProperty("signClass", sign.getClass().getName());
		return ser;
	}

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
