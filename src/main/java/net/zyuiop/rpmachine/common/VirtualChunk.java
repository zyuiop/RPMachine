package net.zyuiop.rpmachine.common;

import net.zyuiop.rpmachine.cities.Line;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public class VirtualChunk {
	private String world = "world";
	private int x;
	private int z;

	public VirtualChunk() {

	}

	public VirtualChunk(Chunk chunk) {
		setX(chunk.getX());
		setZ(chunk.getZ());
		setWorld(chunk.getWorld().getName());
	}

	public VirtualChunk(String world, int x, int z) {
		this.x = x;
		this.z = z;
		this.world = world;
	}

	public Chunk getLocation() {
		return Bukkit.getWorld(world).getChunkAt(x, z);
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public String getWorld() {
		return world;
	}

	public void setWorld(String world) {
		this.world = world;
	}

	public VirtualChunk add(int x, int z) {
		return new VirtualChunk(world, this.x + x, this.z + z);
	}

	public Line line(int startX, int startZ, int endX, int endZ) {
		return new Line(getLocation().getBlock(startX, 64, startZ).getLocation(),
				getLocation().getBlock(endX, 64, endZ).getLocation());
	}

	public VirtualChunk(String string) {
		String[] parts = string.split("/");
		x = Integer.parseInt(parts[0]);
		z = Integer.parseInt(parts[1]);
		world = parts.length > 2 ? parts[2] : "world";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (! (o instanceof VirtualChunk))
			return false;

		VirtualChunk that = (VirtualChunk) o;

		return x == that.x && z == that.z;
	}

	@Override
	public int hashCode() {
		int result = x;
		result = 31 * result + z;
		return result;
	}

	public String toString() {
		return x + "/" + z + "/" + world;
	}
}
