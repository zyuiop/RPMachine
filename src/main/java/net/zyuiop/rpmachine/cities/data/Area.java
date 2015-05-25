package net.zyuiop.rpmachine.cities.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class Area {
	private int min_x;
	private int min_y;
	private int min_z;
	private int max_x;
	private int max_y;
	private int max_z;

	public Area(Location l1, Location l2) {
		min_x = Math.min(l1.getBlockX(), l2.getBlockX());
		min_y = Math.min(l1.getBlockY(), l2.getBlockY());
		min_z = Math.min(l1.getBlockZ(), l2.getBlockZ());
		max_x = Math.max(l1.getBlockX(), l2.getBlockX());
		max_y = Math.max(l1.getBlockY(), l2.getBlockY());
		max_z = Math.max(l1.getBlockZ(), l2.getBlockZ());
	}

	public Area(int min_x, int min_y, int min_z, int max_x, int max_y, int max_z) {
		this.min_x = min_x;
		this.min_y = min_y;
		this.min_z = min_z;
		this.max_x = max_x;
		this.max_y = max_y;
		this.max_z = max_z;
	}

	public Area() {

	}

	public int getMin_x() {
		return min_x;
	}

	public void setMin_x(int min_x) {
		this.min_x = min_x;
	}

	public int getMin_y() {
		return min_y;
	}

	public void setMin_y(int min_y) {
		this.min_y = min_y;
	}

	public int getMin_z() {
		return min_z;
	}

	public void setMin_z(int min_z) {
		this.min_z = min_z;
	}

	public int getMax_x() {
		return max_x;
	}

	public void setMax_x(int max_x) {
		this.max_x = max_x;
	}

	public int getMax_y() {
		return max_y;
	}

	public void setMax_y(int max_y) {
		this.max_y = max_y;
	}

	public int getMax_z() {
		return max_z;
	}

	public void setMax_z(int max_z) {
		this.max_z = max_z;
	}

	public boolean isInside(Location loc) {
		return ((min_x <= loc.getBlockX() && loc.getBlockX() <= max_x) && (min_y <= loc.getBlockY() && loc.getBlockY() <= max_y) && (min_z <= loc.getBlockZ() && loc.getBlockZ() <= max_z));
	}

	public int getSquareArea() {
		return (max_x - min_x) * (max_z - min_z);
	}

	public int getVolume() {
		return getSquareArea() * (max_y - min_y);
	}

	public int getPerimeter() {
		return ((max_x - min_x) * 2) + ((max_z - min_z) * 2);
	}

	public Block getMiddleFloor() {
		int midX = (min_x + max_x) / 2;
		int midZ = (min_z + max_z) / 2;
		return Bukkit.getWorld("world").getHighestBlockAt(midX, midZ);
	}
}
