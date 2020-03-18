package net.zyuiop.rpmachine.common;

import net.zyuiop.rpmachine.common.regions.Region;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.Ownable;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import net.zyuiop.rpmachine.permissions.Permission;
import net.zyuiop.rpmachine.permissions.PlotPermissions;
import net.zyuiop.rpmachine.permissions.PlotPermissionsGui;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Plot implements Ownable {
    private String plotName;
    private Region area;
    private String owner = null;
    private PlotSettings plotSettings = new PlotSettings();
    private Date deletionDate = null;
    // TODO: replace with RoleToken
    private CopyOnWriteArrayList<UUID> plotMembers = new CopyOnWriteArrayList<>();
    private Set<ExternalPlotPermissions> externalPermissions = new HashSet<>();

    public boolean canExternalDo(ExternalPlotPermissions perm) {
        return externalPermissions.contains(perm);
    }

    public void setExternalPermission(ExternalPlotPermissions perm) {
        if (externalPermissions.contains(perm)) externalPermissions.remove(perm);
        else externalPermissions.add(perm);
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

    protected DelegatedPermission buildPermission() {
        return PlotPermissions.BUILD_ON_PLOTS;
    }
    protected DelegatedPermission interactPermission() {
        return PlotPermissions.INTERACT_ON_PLOTS;
    }

    public boolean canBuild(Player player) {
        return canExternalDo(ExternalPlotPermissions.BUILD) ||
                plotMembers.contains(player.getUniqueId()) || // membre du plot
                (owner != null && owner() != null && owner().hasDelegatedPermission(player, buildPermission())); // owner du plot
    }

    public boolean canInteractWithBlock(Player player) {
        return canExternalDo(ExternalPlotPermissions.INTERACT_BLOCKS) ||
                plotMembers.contains(player.getUniqueId()) || // membre du plot
                (owner != null && owner() != null && (owner().hasDelegatedPermission(player, buildPermission()) || owner().hasDelegatedPermission(player, interactPermission()))); // owner du plot
    }

    public boolean canInteractWithEntity(Player player) {
        return canExternalDo(ExternalPlotPermissions.INTERACT_ENTITIES) ||
                plotMembers.contains(player.getUniqueId()) || // membre du plot
                (owner != null && owner() != null && (owner().hasDelegatedPermission(player, buildPermission()) || owner().hasDelegatedPermission(player, interactPermission()))); // owner du plot
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

    public Window externalPermissionsGui(Player player, Runnable save) {
        return new PlotPermissionsGui(player, this, save);
    }

    @Nullable
    @Override
    public String ownerTag() {
        return getOwner();
    }

    public enum ExternalPlotPermissions {
        BUILD("construire sur la parcelle"),
        INTERACT_BLOCKS("intéragir avec les blocs de la parcelle"),
        INTERACT_ENTITIES("intéragir avec les entités de la parcelle");

        private final String description;

        ExternalPlotPermissions(String description) {
            this.description = description;
        }

        public String description() {
            return description;
        }
    }

}
