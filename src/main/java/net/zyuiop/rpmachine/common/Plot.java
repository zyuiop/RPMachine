package net.zyuiop.rpmachine.common;

import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.Ownable;
import net.zyuiop.rpmachine.permissions.PlotPermissions;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Plot implements Ownable {
	private String plotName;
	private Area area;
	private String owner = null;
	private PlotSettings plotSettings = new PlotSettings();
	// TODO: replace with RoleToken
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

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public void setOwner(LegalEntity owner) {
		setOwner(owner.tag());
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
				owner != null && owner() != null && owner().hasDelegatedPermission(player, PlotPermissions.BUILD_ON_PLOTS); // owner du plot
	}

	@Nullable
	@Override
	public String ownerTag() {
		return getOwner();
	}
}
