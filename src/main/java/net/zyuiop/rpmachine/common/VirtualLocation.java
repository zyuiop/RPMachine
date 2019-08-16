package net.zyuiop.rpmachine.common;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class VirtualLocation {
    private String world = "world";
    private int x;
    private int y;
    private int z;

    public VirtualLocation() {

    }

    public VirtualLocation(Location loc) {
        world = loc.getWorld().getName();
        setX(loc.getBlockX());
        setY(loc.getBlockY());
        setZ(loc.getBlockZ());
    }

    public VirtualLocation(String string) {
        String[] worldParts = string.split(" ");
        world = worldParts.length > 1 ? worldParts[0] : "world";
        String[] parts = (worldParts.length > 1 ? worldParts[1] : worldParts[0]).split("/");

        x = Integer.valueOf(parts[0]);
        y = Integer.valueOf(parts[1]);
        z = Integer.valueOf(parts[2]);
    }

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VirtualLocation))
            return false;

        VirtualLocation that = (VirtualLocation) o;
        return x == that.x && y == that.y && z == that.z;

    }

    public String toString() {
        return world + " " + x + "/" + y + "/" + z;
    }
}
