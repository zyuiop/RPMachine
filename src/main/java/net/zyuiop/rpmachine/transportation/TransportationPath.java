package net.zyuiop.rpmachine.transportation;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.Line;
import net.zyuiop.rpmachine.common.VirtualLocation;
import net.zyuiop.rpmachine.database.StoredEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class TransportationPath implements StoredEntity {
    private VirtualLocation startPoint;
    private String name;
    private String displayName;
    private EntityType type;
    private String fileName;
    private Material iconMaterial;
    private final List<VirtualLocation> locations = new ArrayList<>();
    private double price;

    public void proceed(Player player) {
        var loc = startPoint.getLocation();

        var carrier = (Mob) loc.getWorld().spawnEntity(loc, type);

        carrier.addPassenger(player);
        carrier.clearLootTable();
        carrier.setInvulnerable(true);
        carrier.setAI(false);
        carrier.setAware(false);
        carrier.setTarget(null);
        carrier.setCustomNameVisible(true);

        carrier.setCustomName(ChatColor.DARK_AQUA + "Hystori'air");

        carrier.setCollidable(false);

        final List<Location> list = locations.stream().map(VirtualLocation::getLocation).collect(Collectors.toList());
        final Iterator<Location> iterator = list.iterator();

        var task = new BukkitRunnable() {
            private Location currentTarget = loc;
            private Vector currentVector;
            private double prevDistance = 0D;

            @Override
            public void run() {
                if (currentVector == null) {
                    next();
                }

                var dist = carrier.getLocation().distanceSquared(currentTarget);
                if (dist <= 1.0 || dist > prevDistance) {
                    next();
                } else {
                    prevDistance = dist;
                }

                carrier.setVelocity(currentVector);
            }

            private void next() {
                if (!iterator.hasNext()) {
                    carrier.remove();
                    player.sendMessage(ChatColor.GREEN + "Merci d'avoir voyagé avec Histori'air !");
                    cancel();
                } else {
                    var oldTarget = carrier.getLocation();
                    currentTarget = iterator.next();
                    currentVector = currentTarget.toVector().subtract(oldTarget.toVector()).normalize().multiply(0.5);
                    prevDistance = oldTarget.distanceSquared(currentTarget) + 10;


                    var loc = carrier.getLocation().clone().setDirection(currentVector);
                    carrier.setRotation(loc.getYaw(), loc.getPitch());
                }
            }
        };

        task.runTaskTimer(RPMachine.getInstance(), 2, 2);
    }

    public void display(Player p) {
        List<Line> lines = new ArrayList<>();
        var current = startPoint;
        for (var loc: locations) {
            lines.add(new Line(current.getLocation(), loc.getLocation()));
            current = loc;
        }

        lines.forEach(l -> l.displayLine(p));
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String name) {
        this.fileName = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }

    public List<VirtualLocation> getLocations() {
        return locations;
    }

    public VirtualLocation getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(VirtualLocation startPoint) {
        this.startPoint = startPoint;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Material getIconMaterial() {
        return iconMaterial;
    }

    public void setIconMaterial(Material iconMaterial) {
        this.iconMaterial = iconMaterial;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
