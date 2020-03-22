package net.zyuiop.rpmachine.common.selections;

import net.zyuiop.rpmachine.common.regions.CompoundRegion;
import net.zyuiop.rpmachine.common.regions.Region;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CompoundSelection implements Selection<CompoundRegion> {
    private List<Region> regions = new ArrayList<>();
    private Selection<?> current;

    public boolean addSelection() {
        Region r = current.getRegion();
        for (Block block : r) {
            if (regions.stream().anyMatch(region -> region.isInside(block.getLocation()))) {
                return false;
            }
        }

        regions.add(r);
        return true;
    }

    public void resetSelection(Selection<?> newCurrent) {
        this.current = newCurrent;
    }

    @Override
    public CompoundRegion getRegion() {
        return new CompoundRegion(regions);
    }

    @Override
    public void leftClick(Block clicked, Player clicking) {
        this.current.leftClick(clicked, clicking);
    }

    @Override
    public void rightClick(Block clicked, Player clicking) {
        this.current.rightClick(clicked, clicking);
    }

    @Override
    public void expandY(int y) {
        this.current.expandY(y);
    }

    @Override
    public void describe(Player player) {
        player.sendMessage(ChatColor.GRAY + "Sélection multiple contenant :");
        this.regions.forEach(r -> r.describe(player));
        player.sendMessage(ChatColor.YELLOW + "Sélection en cours : (/sel multiple add pour passer à la suivante)");
        this.current.describe(player);
    }
}
