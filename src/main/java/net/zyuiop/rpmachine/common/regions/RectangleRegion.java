package net.zyuiop.rpmachine.common.regions;

import net.zyuiop.rpmachine.cities.Line;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;

public class RectangleRegion implements Region {
	protected String world = "world";
	protected int minX;
	protected int minY;
	protected int minZ;
	protected int maxX;
	protected int maxY;
	protected int maxZ;

	public RectangleRegion(Location l1, Location l2) {
		world = l1.getWorld().getName();
		minX = Math.min(l1.getBlockX(), l2.getBlockX());
		minY = Math.min(255, Math.max(0, Math.min(l1.getBlockY(), l2.getBlockY())));
		minZ = Math.min(l1.getBlockZ(), l2.getBlockZ());
		maxX = Math.max(l1.getBlockX(), l2.getBlockX());
		maxY = Math.min(255, Math.max(0, Math.max(l1.getBlockY(), l2.getBlockY())));
		maxZ = Math.max(l1.getBlockZ(), l2.getBlockZ());
	}

	public RectangleRegion(String world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this.world = world;
		this.minX = minX;
		this.minY = Math.max(0, Math.min(255, minY));
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = Math.max(0, Math.min(255, maxY));;
		this.maxZ = maxZ;
	}

	public Location getFirst() {
		return Bukkit.getWorld(world).getBlockAt(minX, minY, minZ).getLocation();
	}

	public Location getSecond() {
		return Bukkit.getWorld(world).getBlockAt(maxX, maxY, maxZ).getLocation();
	}

	public RectangleRegion() {

	}

	public void expandY(int y) {
		if (y < 0)
			minY += y;
		else
			maxY += y;

		minY = Math.min(255, Math.max(0, minY));
		maxY = Math.min(255, Math.max(0, maxY));
	}

	public int getMinX() {
		return minX;
	}

	public int getMinY() {
		return minY;
	}

	public int getMinZ() {
		return minZ;
	}

	public int getMaxX() {
		return maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public int getMaxZ() {
		return maxZ;
	}

	public boolean isInside(Location loc) {
		return (loc.getWorld().getName().equalsIgnoreCase(world) &&(minX <= loc.getBlockX() && loc.getBlockX() <= maxX) && (minY <= loc.getBlockY() && loc.getBlockY() <= maxY) && (minZ <= loc.getBlockZ() && loc.getBlockZ() <= maxZ));
	}

	@Override
	public int computeArea() {
		return getSquareArea();
	}

	@Override
	public int computeVolume() {
		return getVolume();
	}

	@Deprecated
	public int getSquareArea() {
		return (maxX - minX) * (maxZ - minZ);
	}

	@Deprecated
	public int getVolume() {
		return getSquareArea() * (maxY - minY);
	}

	public int getPerimeter() {
		return ((maxX - minX) * 2) + ((maxZ - minZ) * 2);
	}

	public Block getMiddleFloor() {
		int midX = (minX + maxX) / 2;
		int midZ = (minZ + maxZ) / 2;
		return Bukkit.getWorld(world).getHighestBlockAt(midX, midZ);
	}

	public boolean hasBlockInChunk(Chunk chunk) {
		RectangleRegion chunkArea = new RectangleRegion(chunk.getBlock(0, 0, 0).getLocation(), chunk.getBlock(15, 0, 15).getLocation());

		if (chunkArea.getMaxX() < getMinX() || chunkArea.getMaxZ() < getMaxZ() || chunkArea.getMinX() > getMaxX() || chunkArea.getMinZ() > getMaxZ()) {
			return false;
		}

		return true;
	}

	@Override
	public Iterator<Block> iterator() {
		return new Iterator<Block>() {
			private int x = minX;
			private int y = minY;
			private int z = minZ;

			@Override
			public boolean hasNext() {
				return z <= maxZ;
			}

			@Override
			public Block next() {
				Block b = Bukkit.getWorld(world).getBlockAt(x, y, z);

				x++;
				if (x > maxX) {
					x = minX;
					y++;
				}

				if (y > maxY) {
					y = minY;
					z++;
				}
				return b;
			}
		};
	}

	@Override
	public String toString() {
		return world + "-" + minX + "-" + minY + "-" + minZ + "-" + maxX + "-" + maxY + "-" + maxZ;
	}

	public String getWorld() {
		return world;
	}


	@Override
	public void describe(Player player) {
		player.sendMessage(ChatColor.GRAY + "Rectangle de sommets " + ChatColor.YELLOW + minX + " " + minY + " " + minZ + " à " + maxX + " " + maxY + " " + maxZ);
	}

	private Location getLocation(int x, int y, int z) {
		return Bukkit.getWorld(world).getBlockAt(x, y, z).getLocation();
	}


	@Override
	public List<Line> getBorders() {
		return List.of(
				new Line(getLocation(minX, minY, minZ), getLocation(minX, maxY, maxZ)),
				new Line(getLocation(minX, minY, minZ), getLocation(maxX, maxY, minZ)),

				new Line(getLocation(maxX, minY, maxZ), getLocation(minX, maxY, maxZ)),
				new Line(getLocation(maxX, minY, maxZ), getLocation(maxX, maxY, minZ))
		);
	}
}
