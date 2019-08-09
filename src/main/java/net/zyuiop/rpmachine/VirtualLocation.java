package net.zyuiop.rpmachine;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class VirtualLocation {
	private String world = "world";
	private int x;
	private int y;
	private int z;

	public VirtualLocation() {

	}

	public VirtualLocation(Location loc) {
		world = loc.getWorld().getName();
		setX(loc.getBlockX());
		setY(loc.getBlockY());
		setZ(loc.getBlockZ());
	}

	public Location getLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public VirtualLocation(String string) {
		String[] parts = string.split("/");
		x = Integer.valueOf(parts[0]);
		y = Integer.valueOf(parts[1]);
		z = Integer.valueOf(parts[2]);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof VirtualLocation))
			return false;

		VirtualLocation that = (VirtualLocation) o;
		return x == that.x && y == that.y && z == that.z;

	}

	public String toString() {
		return x + "/" + y + "/" + z;
	}
}
