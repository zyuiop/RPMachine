package net.zyuiop.rpmachine.shops;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.database.filestorage.FileEntityStore;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.shops.types.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

import javax.swing.plaf.SplitPaneUI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ShopsManager extends FileEntityStore<AbstractShopSign> {
    protected final ConcurrentHashMap<Location, AbstractShopSign> signs = new ConcurrentHashMap<>();
    protected final Map<String, ShopBuilder<? extends AbstractShopSign>> shopBuilders = new HashMap<>();

    public ShopsManager() {
        super(AbstractShopSign.class, "shops");

        super.load();

        registerShopBuilder(new PlotSign.Builder(), "PlotShop", "[PlotShop]", "[Plot]", "[Parcelle]");
        registerShopBuilder(new ItemShopSign.Builder(), "Shop", "ItemShop", "[Shop]", "[Boutique]");
        registerShopBuilder(new TollShopSign.Builder(), "TollShop", "[TollShop]", "[Toll]", "[Peage]", "[Péage]");
        registerShopBuilder(new EnchantingSign.Builder(), "EnchantShop", "ShopEnchant", "[Enchantment]", "[Enchantement]", "[Enchant]");
    }

    private void registerShopBuilder(ShopBuilder<? extends AbstractShopSign> builder, String... validFirstLines) {
        for (String f : validFirstLines)
            shopBuilders.put(f.toLowerCase(), builder);
    }

    public final void buildShop(SignChangeEvent event) {
        RoleToken tt = RPMachine.getPlayerRoleToken(event.getPlayer());
        String type = event.getLine(0);

        if (type == null)
            return;

        ShopBuilder<? extends AbstractShopSign> builder = shopBuilders.get(type.toLowerCase());
        if (builder == null)
            return;

        // Try to build the shop
        try {
            Optional<? extends AbstractShopSign> sign = builder.parseSign(event.getBlock(), tt, event.getLines());

            if (sign.isPresent()) {
                create(sign.get());
                builder.postCreateInstructions(event.getPlayer());
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "Format de panneau invalide.");
                event.getBlock().breakNaturally();
                builder.describeFormat(event.getPlayer());
            }
        } catch (ShopBuilder.SignParseError e) {
            event.getBlock().breakNaturally();
            event.getPlayer().sendMessage(ChatColor.RED + "Format de panneau invalide : " + e.getMessage());
            builder.describeFormat(event.getPlayer());
        } catch (ShopBuilder.SignPermissionError e) {
            event.getPlayer().sendMessage(ChatColor.RED + "Permission manquante (en tant que " + tt.getLegalEntity().displayable() + ChatColor.RED + ") : " + e.getMessage());
            event.getBlock().breakNaturally();
        }
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

    public final Set<AbstractShopSign> getPlayerShops(Player player) {
        return signs.values().stream().filter(shopSign -> shopSign.owner() instanceof PlayerData && ((PlayerData) shopSign.owner()).getUuid().equals(player.getUniqueId())).collect(Collectors.toSet());
    }

    public final Set<AbstractShopSign> getShops(LegalEntity payer) {
        String token = payer.tag();
        return signs.values().stream().filter(sign -> sign.getOwner().equals(token)).collect(Collectors.toSet());
    }

}
