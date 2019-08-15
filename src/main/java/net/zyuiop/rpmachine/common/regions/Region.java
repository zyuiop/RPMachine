package net.zyuiop.rpmachine.common.regions;

import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * @author Louis Vialar
 */
public interface Region extends Iterable<Block> {
    boolean isInside(Location location);

    int computeArea();

    int computeVolume();
}
