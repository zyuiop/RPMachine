package net.zyuiop.rpmachine.common.selections;

import net.zyuiop.rpmachine.common.regions.RectangleRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class RectangleSelection implements Selection<RectangleRegion> {
    private Location location1;
    private Location location2;

    public RectangleSelection() {

    }

    @Deprecated
    public Location getLocation1() {
        return location1;
    }

    @Deprecated
    public void setLocation1(Location location1) {
        this.location1 = location1;
    }

    @Deprecated
    public Location getLocation2() {
        return location2;
    }

    @Deprecated
    public void setLocation2(Location location2) {
        this.location2 = location2;
    }

    @Deprecated
    public RectangleRegion getArea() {
        return new RectangleRegion(location1, location2);
    }

    @Override
    public RectangleRegion getRegion() {
        if (location1 == null || location2 == null)
            throw new IllegalArgumentException("Les deux points n'ont pas été définis.");
        if (!location1.getWorld().getName().equals(location2.getWorld().getName()))
            throw new IllegalArgumentException("Les deux points ne sont pas dans le même monde.");
        return new RectangleRegion(location1, location2);
    }

    @Override
    public void leftClick(Block block, Player player) {
        location1 = block.getLocation();
        player.sendMessage(ChatColor.GREEN + "Position #1 définie aux coordonnées " + block.getX() + "; " + block.getY() + "; " + block.getZ());
    }

    @Override
    public void rightClick(Block block, Player player) {
        location2 = block.getLocation();
        player.sendMessage(ChatColor.GREEN + "Position #2 définie aux coordonnées " + block.getX() + "; " + block.getY() + "; " + block.getZ());
    }

    @Override
    public void expandY(int y) {
        Location toUpdate = location1.getY() < location2.getY() ? (y < 0 ? location1 : location2) : (y < 0 ? location2 : location1); // get minY or maxY depending on y param
        toUpdate.setY(toUpdate.getY() + y);
    }

    @Override
    public void describe(Player player) {
        player.sendMessage(ChatColor.GRAY + "Rectangle de sommets " + ChatColor.YELLOW +
                (location1 != null ? (location1.getBlockX() + " " + location1.getBlockY() + " " + location1.getBlockZ()) : "<indéfini>") +
                ChatColor.GRAY + " à " + ChatColor.YELLOW +
                (location2 != null ? (location2.getBlockX() + " " + location2.getBlockY() + " " + location2.getBlockZ()) : "<indéfini>")
        );
    }
}
