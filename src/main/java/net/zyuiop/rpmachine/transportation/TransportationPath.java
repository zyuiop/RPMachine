package net.zyuiop.rpmachine.transportation;

import net.zyuiop.rpmachine.cities.Line;
import net.zyuiop.rpmachine.common.VirtualLocation;
import net.zyuiop.rpmachine.database.StoredEntity;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TransportationPath implements StoredEntity {
    private final List<VirtualLocation> locations = new ArrayList<>();
    private VirtualLocation startPoint;
    private String name;
    private String displayName;
    private EntityType type;
    private String fileName;
    private Material iconMaterial;
    private double price;

    public void display(Player p) {
        List<Line> lines = new ArrayList<>();
        var current = startPoint;
        for (var loc : locations) {
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
