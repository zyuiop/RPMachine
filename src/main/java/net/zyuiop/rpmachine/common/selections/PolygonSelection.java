package net.zyuiop.rpmachine.common.selections;

import net.zyuiop.rpmachine.common.regions.PolygonRegion;
import net.zyuiop.rpmachine.common.regions.RectangleRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public class PolygonSelection implements Selection<PolygonRegion> {
	private List<Location> locations;

	public PolygonSelection(Location l1, Location l2) {
		this.location1 = l1;
		this.location2 = l2;
	}

	public PolygonSelection() {

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
		player.sendMessage(ChatColor.GREEN + "Position #1 définie aux coordonnées " + block.getX() +"; "+ block.getY() + "; " + block.getZ());
	}

	@Override
	public void rightClick(Block block, Player player) {
		location2 = block.getLocation();
		player.sendMessage(ChatColor.GREEN + "Position #2 définie aux coordonnées " + block.getX() +"; "+ block.getY() + "; " + block.getZ());
	}
}
