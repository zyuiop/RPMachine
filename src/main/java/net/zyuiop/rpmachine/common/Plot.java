package net.zyuiop.rpmachine.common;

import net.zyuiop.rpmachine.claims.Claim;
import net.zyuiop.rpmachine.claims.CompoundClaim;
import net.zyuiop.rpmachine.common.regions.Region;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.Ownable;
import net.zyuiop.rpmachine.json.JsonExclude;
import net.zyuiop.rpmachine.permissions.PlotPermissions;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Plot extends CompoundClaim implements Ownable {
    private String plotName;
    private Region area;
    private String owner = null;
    private PlotSettings plotSettings = new PlotSettings();
    private Date deletionDate = null;
    // TODO: allow subplots
    // TODO: replace with RoleToken
    private CopyOnWriteArrayList<UUID> plotMembers = new CopyOnWriteArrayList<>();

    private boolean hasPlotPermission(Player player) {
        return plotMembers.contains(player.getUniqueId()) || // membre du plot
                hasOwnerPermission(player); // owner du plot
    }

    protected boolean hasOwnerPermission(Player player) {
        return owner != null && owner() != null && owner().hasDelegatedPermission(player, PlotPermissions.BUILD_ON_PLOTS);
    }

    // TODO: allow customization for external players
    @Override
    public boolean canBuild(Player player, Location location) {
        return super.canBuild(player, location) || hasPlotPermission(player);
    }

    @Override
    public boolean canInteractWithBlock(Player player, Block block, Action action) {
        return super.canInteractWithBlock(player, block, action) || hasPlotPermission(player);
    }

    @Override
    public boolean canInteractWithEntity(Player player, Entity entity) {
        return super.canInteractWithEntity(player, entity) || hasPlotPermission(player);
    }

    @Override
    public boolean canDamageEntity(Player player, Entity entity) {
        return super.canDamageEntity(player, entity) || hasPlotPermission(player);
    }

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

    public Region getArea() {
        return area;
    }

    public void setArea(Region area) {
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

    public Date getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(Date deletionDate) {
        this.deletionDate = deletionDate;
    }

    public String getDeletionDateString() {
        return deletionDate == null ? null : SimpleDateFormat.getDateTimeInstance().format(getDeletionDate());
    }

    public void setForDeletion() {
        long time = 24L * 7L * 3600L * 1000L;
        time += System.currentTimeMillis();
        this.deletionDate = new Date(time);
    }

    public boolean isDueForDeletion() {
        return this.deletionDate != null;
    }

    @Override
    public boolean isInside(Location location) {
        return area.isInside(location);
    }

    public void sendDeletionWarning(String cityName) {
        if (owner() != null) {
            Messages.sendMessage(owner(), ChatColor.RED + "Attention ! La parcelle " + ChatColor.DARK_RED + plotName + ChatColor.RED +
                    " située en ville de " + ChatColor.DARK_RED + cityName + ChatColor.RED + " sera supprimée le " + ChatColor.DARK_RED + getDeletionDateString() +
                    ChatColor.RED + "."
            );

            Messages.sendMessage(owner(), ChatColor.GRAY + "Vous pouvez quitter la parcelle pour la supprimer immédiatement.");
        }
    }

    @Nullable
    @Override
    public String ownerTag() {
        return getOwner();
    }

    @Override @JsonExclude
    public Collection<Claim> getClaims() {
        return Collections.emptySet(); // TODO: make it possible to create subplots
    }
}
