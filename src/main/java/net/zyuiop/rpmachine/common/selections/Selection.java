package net.zyuiop.rpmachine.common.selections;

import net.zyuiop.rpmachine.common.regions.Region;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public interface Selection<T extends Region> {
    T getRegion();

    void leftClick(Block clicked, Player clicking);

    void rightClick(Block clicked, Player clicking);
}
