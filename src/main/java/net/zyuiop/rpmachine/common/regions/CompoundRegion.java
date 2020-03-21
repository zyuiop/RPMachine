package net.zyuiop.rpmachine.common.regions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import net.zyuiop.rpmachine.cities.Line;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class CompoundRegion implements Region {
    private final List<Region> regions;

    public CompoundRegion(List<Region> regions) {
        this.regions = Collections.unmodifiableList(regions);
    }

    @Override
    public boolean isInside(Location location) {
        return regions.stream().anyMatch(r -> r.isInside(location));
    }

    @Override
    public int computeArea() {
        return regions.stream().mapToInt(Region::computeArea).sum();
    }

    @Override
    public int computeVolume() {
        return regions.stream().mapToInt(Region::computeVolume).sum();
    }

    @Override
    public boolean hasBlockInChunk(Chunk chunk) {
        return regions.stream().anyMatch(r -> r.hasBlockInChunk(chunk));
    }

    @Override
    public void expandY(int y) {
        regions.forEach(r -> r.expandY(y));
    }

    @Override
    public void describe(Player player) {
        player.sendMessage(ChatColor.GRAY + "Regroupement de plusieurs régions : ");
        regions.forEach(r -> r.describe(player));
    }

    @Override
    public List<Line> getBorders() {
        return regions.stream().flatMap(r -> r.getBorders().stream()).collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Iterator<Block> iterator() {
        return Iterators.concat(regions.stream().map(Iterable::iterator).collect(Collectors.toList()).iterator());
    }
}
