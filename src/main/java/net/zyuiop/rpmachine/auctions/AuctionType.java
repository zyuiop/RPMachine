package net.zyuiop.rpmachine.auctions;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.MenuItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author Louis Vialar
 */
public class AuctionType {
    private String id;
    private String name;
    private Set<Material> materials = new HashSet<>();

    public AuctionType(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String[] getSignName() {
        String name = this.name;
        if (name.length() > 16) {
            if (name.length() > 32)
                name = StringUtils.abbreviate(name, 32);

            // Try split
            String[] parts = name.split(" ");
            if (parts.length == 1 || (parts.length == 2 && (parts[0].length() > 16 || parts[1].length() > 16))) {
                return new String[]{name.substring(0, 16), name.substring(16)};
            } else if (parts.length == 2) {
                return parts;
            } else {
                // Try all combinations, and return as soon as left becomes too long
                String prevLeft = name.substring(0, 16);
                String prevRight = name.substring(16);

                for (int i = 1; i < parts.length; ++i) {
                    String left = StringUtils.join(parts, " ", 0, i);
                    String right = StringUtils.join(parts, " ", i, parts.length);

                    if (left.length() > 16) {
                        return new String[] { prevLeft, prevRight };
                    } else {
                        prevLeft = left;
                        prevRight = StringUtils.abbreviate(right, 16); // just in case
                    }

                }

                return new String[] { prevLeft, prevRight };
            }
        } else return new String[]{name};
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Material> getMaterials() {
        return materials;
    }

    public Window createWindow(Player player) {
        return new Window(Window.computeSize(materials.size()), "Choisir l'item à échanger", player) {
            @Override
            public void fill() {
               Material[] materials = AuctionType.this.materials.toArray(new Material[0]);
                for (int i = 0; i < materials.length && i < size; ++i) {
                    Material mat = materials[i];
                    double minPrice = AuctionManager.INSTANCE.minPrice(mat);
                    double avgPrice = AuctionManager.INSTANCE.averagePrice(mat);
                    int avail = AuctionManager.INSTANCE.countAvailable(mat);
                    setItem(i, new MenuItem(mat).setName(mat.name()).setDescription(
                            ChatColor.YELLOW + "Prix moyen: " + (avgPrice != avgPrice ? ChatColor.RED + "Inconnu" : ChatColor.AQUA + String.format("%.2f", avgPrice)) + RPMachine.getCurrencyName(),
                            ChatColor.YELLOW + "Prix minimum: " + (minPrice != minPrice ? ChatColor.RED + "Inconnu" : ChatColor.AQUA + String.format("%.2f", minPrice)) + RPMachine.getCurrencyName(),
                            ChatColor.YELLOW + "Disponible: " + avail
                    ), () -> {
                        close();
                        new ItemAuctionGui(mat, player).open();
                    });
                }
            }
        };
    }
}
