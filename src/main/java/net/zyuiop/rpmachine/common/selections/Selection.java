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

    /**
     * Expand the selection in height
     * @param y the height to add, if negative height will be added at the bottom
     */
    void expandY(int y);

    void describe(Player player);
}
