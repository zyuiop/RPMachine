package net.zyuiop.rpmachine.common.regions;

import net.zyuiop.rpmachine.common.VirtualLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
public class PolygonRegion extends RectangleRegion {
    private List<VirtualLocation> points;

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

        // Update X and Z
        for (VirtualLocation v : points) {
            int x = v.getX();
            int z = v.getZ();
            if (x < minX) minX = x;
            if (z < minZ) minZ = z;
            if (x > maxX) maxX = x;
            if (z > maxZ) maxZ = z;
        }

        // Update Y
        int oldMinY = minY;
        int oldMaxY = maxY;
        minY = Math.min(oldMinY, oldMaxY);
        maxY = Math.max(oldMinY, oldMaxY);

        World world = this.world == null ? null : Bukkit.getWorld(this.world);
        minY = Math.min(Math.max(0, minY), world == null ? 255 : world.getMaxHeight());
        maxY = Math.min(Math.max(0, maxY), world == null ? 255 : world.getMaxHeight());
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
        // Check word
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

    @Override
    public Iterator<Block> iterator() {
        return null;
    }
}
