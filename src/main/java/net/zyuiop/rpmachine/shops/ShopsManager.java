package net.zyuiop.rpmachine.shops;

import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.database.filestorage.FileEntityStore;
import net.zyuiop.rpmachine.entities.LegalEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ShopsManager extends FileEntityStore<AbstractShopSign> {
    protected final ConcurrentHashMap<Location, AbstractShopSign> signs = new ConcurrentHashMap<>();

    public ShopsManager() {
        super(AbstractShopSign.class, "shops");

        super.load();
    }

    public final void create(AbstractShopSign sign) {
        signs.put(sign.getLocation(), sign);
        sign.display();

        String name = locAsString(sign.getLocation());
        super.createEntity(name, sign);
    }

    @Override
    protected void loadedEntity(AbstractShopSign entity) {
        entity.display();
        signs.put(entity.getLocation(), entity);
    }

    protected String locAsString(Location loc) {
        return loc.getWorld().getName() + "-" + loc.getBlockX() + "-" + loc.getBlockY() + "-" + loc.getBlockZ();
    }

    protected Location locFromString(String loc) {
        String[] parts = loc.split("-");
        return new Location(Bukkit.getWorld(parts[0]), Integer.valueOf(parts[1]), Integer.valueOf(parts[2]), Integer.valueOf(parts[3]));
    }

    public final void remove(AbstractShopSign shopSign) {
        super.removeEntity(shopSign);
        signs.remove(shopSign.getLocation());
    }

    public final AbstractShopSign get(Location location) {
        return signs.get(location);
    }

    public void save(AbstractShopSign shopSign) {
        super.saveEntity(shopSign);
    }

    public final HashSet<ItemShopSign> getPlayerShops(Player player) {
        return signs.values().stream().filter(shopSign -> shopSign.owner() instanceof PlayerData && ((PlayerData) shopSign.owner()).getUuid().equals(player.getUniqueId()) && shopSign instanceof ItemShopSign).map(shopSign -> (ItemShopSign) shopSign).collect(Collectors.toCollection(HashSet::new));
    }

    public final Set<AbstractShopSign> getShops(LegalEntity payer) {
        String token = payer.tag();
        return signs.values().stream().filter(sign -> sign.getOwner().equals(token)).collect(Collectors.toSet());
    }

}
