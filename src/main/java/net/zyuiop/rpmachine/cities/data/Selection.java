package net.zyuiop.rpmachine.cities.data;

import org.bukkit.Location;

public class Selection {
	private Location location1;
	private Location location2;

	public Selection(Location l1, Location l2) {
		this.location1 = l1;
		this.location2 = l2;
	}

	public Selection() {

	}

	public Location getLocation1() {
		return location1;
	}

	public void setLocation1(Location location1) {
		this.location1 = location1;
	}

	public Location getLocation2() {
		return location2;
	}

	public void setLocation2(Location location2) {
		this.location2 = location2;
	}

	public Area getArea() {
		return new Area(location1, location2);
	}
}
