package net.zyuiop.rpmachine.common;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class Area {
	private String world = "world";
	private int minX;
	private int minY;
	private int minZ;
	private int maxX;
	private int maxY;
	private int maxZ;

	public Area(Location l1, Location l2) {
		world = l1.getWorld().getName();
		minX = Math.min(l1.getBlockX(), l2.getBlockX());
		minY = Math.min(l1.getBlockY(), l2.getBlockY());
		minZ = Math.min(l1.getBlockZ(), l2.getBlockZ());
		maxX = Math.max(l1.getBlockX(), l2.getBlockX());
		maxY = Math.max(l1.getBlockY(), l2.getBlockY());
		maxZ = Math.max(l1.getBlockZ(), l2.getBlockZ());
	}

	public Area(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public Area(String world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
		this.world = world;
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	public Area() {

	}

	public int getMinX() {
		return minX;
	}

	public void setMinX(int minX) {
		this.minX = minX;
	}

	public int getMinY() {
		return minY;
	}

	public void setMinY(int minY) {
		this.minY = minY;
	}

	public int getMinZ() {
		return minZ;
	}

	public void setMinZ(int minZ) {
		this.minZ = minZ;
	}

	public int getMaxX() {
		return maxX;
	}

	public void setMaxX(int maxX) {
		this.maxX = maxX;
	}

	public int getMaxY() {
		return maxY;
	}

	public void setMaxY(int maxY) {
		this.maxY = maxY;
	}

	public int getMaxZ() {
		return maxZ;
	}

	public void setMaxZ(int maxZ) {
		this.maxZ = maxZ;
	}

	public boolean isInside(Location loc) {
		return (loc.getWorld().getName().equalsIgnoreCase(world) &&(minX <= loc.getBlockX() && loc.getBlockX() <= maxX) && (minY <= loc.getBlockY() && loc.getBlockY() <= maxY) && (minZ <= loc.getBlockZ() && loc.getBlockZ() <= maxZ));
	}

	public int getSquareArea() {
		return (maxX - minX) * (maxZ - minZ);
	}

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

	public boolean hasCommonPositionsWith(Chunk chunk) {
		Area chunkArea = new Area(chunk.getBlock(0, 0, 0).getLocation(), chunk.getBlock(15, 0, 15).getLocation());

		if (chunkArea.getMaxX() < getMinX() || chunkArea.getMaxZ() < getMaxZ() || chunkArea.getMinX() > getMaxZ() || chunkArea.getMinZ() > getMaxZ()) {
			return false;
		}

		return true;
	}

	public Area getFlatArea() {
		return new Area(minX, 1, minZ, maxX, 1, maxZ);
	}
}
