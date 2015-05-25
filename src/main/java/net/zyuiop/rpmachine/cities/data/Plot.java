package net.zyuiop.rpmachine.cities.data;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class Plot {
	private String plotName;
	private Area area;
	private UUID owner = null;
	private PlotSettings plotSettings = new PlotSettings();
	private CopyOnWriteArrayList<UUID> plotMembers = new CopyOnWriteArrayList<>();

	public CopyOnWriteArrayList<UUID> getPlotMembers() {
		return plotMembers;
	}

	public void setPlotMembers(CopyOnWriteArrayList<UUID> plotMembers) {
		this.plotMembers = plotMembers;
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	public UUID getOwner() {
		return owner;
	}

	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	public PlotSettings getPlotSettings() {
		return plotSettings;
	}

	public void setPlotSettings(PlotSettings plotSettings) {
		this.plotSettings = plotSettings;
	}

	public Block getMiddleFloor() {
		return area.getMiddleFloor();
	}

	public boolean canBuild(Player player, Location location) {
		if (plotMembers.contains(player.getUniqueId()))
			return true;

		if (owner != null && owner.equals(player.getUniqueId()))
			return true;

		return false;
	}
}
