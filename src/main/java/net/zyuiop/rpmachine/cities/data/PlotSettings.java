package net.zyuiop.rpmachine.cities.data;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class PlotSettings {

	private boolean allowOpenDoors = true;
	private boolean allowRedstone = true;
	private boolean allowSigns = true;

	public PlotSettings(boolean allowOpenDoors, boolean allowRedstone, boolean allowSigns) {
		this.allowOpenDoors = allowOpenDoors;
		this.allowRedstone = allowRedstone;
		this.allowSigns = allowSigns;
	}

	public PlotSettings() {
	}

	public boolean isAllowOpenDoors() {
		return allowOpenDoors;
	}

	public void setAllowOpenDoors(boolean allowOpenDoors) {
		this.allowOpenDoors = allowOpenDoors;
	}

	public boolean isAllowRedstone() {
		return allowRedstone;
	}

	public void setAllowRedstone(boolean allowRedstone) {
		this.allowRedstone = allowRedstone;
	}

	public boolean isAllowSigns() {
		return allowSigns;
	}

	public void setAllowSigns(boolean allowSigns) {
		this.allowSigns = allowSigns;
	}
}
