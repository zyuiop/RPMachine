package net.zyuiop.rpmachine.common.regions;

import net.zyuiop.rpmachine.cities.Line;
import net.zyuiop.rpmachine.common.VirtualLocation;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Louis Vialar
 */
public class PolygonRegion extends RectangleRegion {
    private List<VirtualLocation> points = new ArrayList<>();

    public PolygonRegion() {
        super();
    }

    public PolygonRegion(PolygonRegion copy) {
        this.points = new ArrayList<>(copy.points);
        this.world = copy.world;

        this.computeBoundingSquare();
    }

    @Override
    public boolean isInside(Location location) {
        // Taken from
        // https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/regions/Polygonal2DRegion.java

        // Maybe check the lines in that case?
        if (points.size() < 3)
            return false;

        // Check if the point is in the bounding box
        if (!super.isInside(location))
            return false;

        // height checked
        int targetX = location.getBlockX(); //wide
        int targetZ = location.getBlockZ(); //depth

        boolean inside = false;
        int npoints = points.size();
        int xNew, zNew;
        int xOld, zOld;
        int x1, z1;
        int x2, z2;
        long crossproduct;

        xOld = points.get(npoints - 1).getX();
        zOld = points.get(npoints - 1).getZ();

        for (VirtualLocation point : points) {
            xNew = point.getX();
            zNew = point.getZ();
            //Check for corner
            if (xNew == targetX && zNew == targetZ) {
                return true;
            }
            if (xNew > xOld) {
                x1 = xOld;
                x2 = xNew;
                z1 = zOld;
                z2 = zNew;
            } else {
                x1 = xNew;
                x2 = xOld;
                z1 = zNew;
                z2 = zOld;
            }

            if (x1 <= targetX && targetX <= x2) {
                crossproduct = ((long) targetZ - (long) z1) * (long) (x2 - x1) - ((long) z2 - (long) z1) * (long) (targetX - x1);
                if (crossproduct == 0) {
                    if ((z1 <= targetZ) == (targetZ <= z2))
                        return true; //on edge
                } else if (crossproduct < 0 && (x1 != targetX)) {
                    inside = !inside;
                }
            }
            xOld = xNew;
            zOld = zNew;
        }

        return inside;
    }

    private void computeBoundingSquare() {
        if (points.isEmpty()) {
            throw new IllegalArgumentException("Not enough points");
        }

        this.world = points.get(0).getWorld();

        int minX = points.get(0).getX();
        int maxX = points.get(0).getX();
        int minY = points.get(0).getY();
        int maxY = points.get(0).getY();
        int minZ = points.get(0).getZ();
        int maxZ = points.get(0).getZ();

        // Update X and Z
        for (VirtualLocation v : points) {
            int x = v.getX();
            int z = v.getZ();
            int y = v.getY();
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
            if (z > maxZ) maxZ = z;

            if (!v.getWorld().equals(this.world)) {
                throw new IllegalArgumentException("Un point n'est pas dans le bon monde (monde: " + this.world + ", point fautif: " + v + ")");
            }
        }

        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;

        // Update Y
        World world = this.world == null ? null : Bukkit.getWorld(this.world);
        this.minY = Math.min(Math.max(0, minY), world == null ? 255 : world.getMaxHeight());
        this.maxY = Math.min(Math.max(0, maxY), world == null ? 255 : world.getMaxHeight());
    }

    public void addPoint(VirtualLocation location) {
        if (!getWorld().equals(location.getWorld()))
            throw new IllegalArgumentException("Given location is not in the same world as the rest of the area");

        points.add(location);
        computeBoundingSquare();
    }

    public void addPoint(Location location) {
        addPoint(new VirtualLocation(location));
    }

    public void addPoints(List<Location> locations) {
        points.addAll(locations.stream().map(VirtualLocation::new).collect(Collectors.toList()));
        computeBoundingSquare();
    }

    @Override
    public int computeArea() {
        // Taken from
        // https://github.com/EngineHub/WorldEdit/blob/master/worldedit-core/src/main/java/com/sk89q/worldedit/regions/Polygonal2DRegion.java
        double area = 0;
        int i, j = points.size() - 1;

        for (i = 0; i < points.size(); ++i) {
            area += (points.get(j).getX() + points.get(i).getX()) * (points.get(j).getZ() - points.get(i).getZ());
            j = i;
        }

        return (int) Math.floor(Math.abs(area * 0.5));
    }

    @Override
    public int computeVolume() {
        return computeArea() * (maxY - minY + 1);
    }

    public boolean hasBlockInChunk(Chunk chunk) {
        if (!super.hasBlockInChunk(chunk))
            return false;

        RectangleRegion region = new RectangleRegion(chunk.getBlock(0, 0, 0).getLocation(), chunk.getBlock(0, 255, 0).getLocation());
        return StreamSupport.stream(region.spliterator(), false).anyMatch(block -> isInside(block.getLocation()));
    }

    @Override
    public Iterator<Block> iterator() {
        Iterator<Block> src = super.iterator();
        return new Iterator<Block>() {
            private Block next;

            private void findNext() {
                while (src.hasNext()) {
                    this.next = src.next();
                    if (isInside(next.getLocation()))
                        return;
                }

                this.next = null;
            }

            {
                findNext();
            }

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Block next() {
                Block next = this.next;
                findNext();
                return next;
            }
        };
    }

    @Override
    public void describe(Player player) {
        player.sendMessage(ChatColor.GRAY + "Polygone d'altitude " + minY + " à " + maxY + " et de sommets :");
        this.points.forEach(loc ->
                player.sendMessage(ChatColor.GRAY + Symbols.ARROW_RIGHT_FULL + " " + ChatColor.YELLOW +
                        loc.getX() + " " + loc.getZ()));
    }

    @Override
    public List<Line> getBorders() {
        var iter = points.iterator();
        var first = iter.next();
        var current = first.getLocation();
        var lines = new ArrayList<Line>();

        while (iter.hasNext()) {
            current.setY(minY);
            var next = iter.next().getLocation();
            next.setY(maxY);

            lines.add(new Line(current.clone(), next.clone()));
            current = next;
        }

        var firstLoc = first.getLocation();
        firstLoc.setY(maxY);
        current.setY(minY);
        lines.add(new Line(current.clone(), firstLoc.clone()));
        return lines;
    }
}
