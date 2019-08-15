package net.zyuiop.rpmachine.common.selections;

import net.zyuiop.rpmachine.common.regions.PolygonRegion;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PolygonSelection implements Selection<PolygonRegion> {
    private List<Location> locations = new ArrayList<>();
    private int downYmodif = 0;
    private int upYmodif = 0;

    public PolygonSelection() {
    }

    @Override
    public PolygonRegion getRegion() {
        if (locations.size() < 3)
            throw new IllegalArgumentException("Sélectionnez au moins 3 points");

        PolygonRegion region = new PolygonRegion();
        region.addPoints(locations);
        region.expandY(downYmodif);
        region.expandY(upYmodif);
        return region;
    }

    @Override
    public void leftClick(Block block, Player player) {
        downYmodif = 0;
        upYmodif = 0;
        locations.clear();
        locations.add(block.getLocation());
        player.sendMessage(ChatColor.GREEN + "Sommet #1 défini aux coordonnées " + block.getX() + "; " + block.getY() + "; " + block.getZ());
        player.sendMessage(ChatColor.YELLOW + "Cliquez droit pour ajouter des sommets au polygone.");
    }

    @Override
    public void rightClick(Block block, Player player) {
        locations.add(block.getLocation());
        int i = locations.size();
        player.sendMessage(ChatColor.GREEN + "Sommet #" + i + " défini aux coordonnées " + block.getX() + "; " + block.getY() + "; " + block.getZ());
    }

    @Override
    public void expandY(int y) {
        if (y < 0)
            downYmodif += y;
        else upYmodif += y;
    }

    @Override
    public void describe(Player player) {
        player.sendMessage(ChatColor.GRAY + "Polygone de sommets :");
        this.locations.forEach(loc ->
                player.sendMessage(ChatColor.GRAY + Symbols.ARROW_RIGHT_FULL + " " + ChatColor.YELLOW +
                        loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()));
    }
}
