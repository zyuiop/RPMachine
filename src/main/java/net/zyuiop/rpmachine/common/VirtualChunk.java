package net.zyuiop.rpmachine.common;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public class VirtualChunk {
	private int x;
	private int z;

	public VirtualChunk() {

	}

	public VirtualChunk(Chunk chunk) {
		setX(chunk.getX());
		setZ(chunk.getZ());
	}

	public VirtualChunk(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public Chunk getLocation() {
		return Bukkit.getWorld("world").getChunkAt(x, z);
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

	public VirtualChunk(String string) {
		String[] parts = string.split("/");
		x = Integer.valueOf(parts[0]);
		z = Integer.valueOf(parts[1]);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (! (o instanceof VirtualChunk))
			return false;

		VirtualChunk that = (VirtualChunk) o;

		if (x != that.x)
			return false;
		if (z != that.z)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = x;
		result = 31 * result + z;
		return result;
	}

	public String toString() {
		return x + "/" + z;
	}
}
