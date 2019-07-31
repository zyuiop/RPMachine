package net.zyuiop.rpmachine.common;

import net.zyuiop.rpmachine.economy.TaxPayer;
import net.zyuiop.rpmachine.economy.TaxPayerToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Plot {
	private String plotName;
	private Area area;
	private TaxPayerToken owner = null;
	private PlotSettings plotSettings = new PlotSettings();
	// TODO: replace with TaxPayerToken
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

	public TaxPayerToken getOwner() {
		return owner;
	}

	public void setOwner(TaxPayerToken owner) {
		this.owner = owner;
	}

	public void setOwner(TaxPayer owner) {
		setOwner(TaxPayerToken.fromPayer(owner));
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
		return plotMembers.contains(player.getUniqueId()) || // membre du plot
				owner != null && owner.getLandOwner() != null && owner.getLandOwner().canManagePlot(player); // owner du plot
	}
}
