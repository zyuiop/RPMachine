package net.zyuiop.rpmachine.projects;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.common.regions.RectangleRegion;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.common.regions.Region;
import net.zyuiop.rpmachine.database.StoredEntity;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import net.zyuiop.rpmachine.permissions.Permission;
import net.zyuiop.rpmachine.permissions.ProjectPermissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.StreamSupport;

/**
 * @author zyuiop
 * A zone is a kind of plot located outside of any city. It can only be created with administrative permissions.<br/>
 * A zone has no taxes as it doesn't depend of any city.
 */
public class Project extends Plot implements LegalEntity, StoredEntity {
    private final Map<UUID, Set<Permission>> admins = new HashMap<>();
    private String fileName;
    private double money = 0D;
    private Map<String, Double> unpaidTaxes = new HashMap<>();
    private Map<String, Date> lastPaidTaxes = new HashMap<>();
    private boolean allowCityCreate = false;

    public String getFileName() {
        return fileName;
    }

    public boolean isAllowCityCreate() {
        return allowCityCreate;
    }

    public void setAllowCityCreate(boolean allowCityCreate) {
        this.allowCityCreate = allowCityCreate;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean checkArea(Region area, ProjectsManager manager, Player player) {
        boolean cancel = StreamSupport.stream(area.spliterator(), false)
                .anyMatch(block -> {
                    if (RPMachine.getInstance().getCitiesManager().getCityHere(block.getChunk()) != null) {
                        player.sendMessage(ChatColor.RED + "Une partie de votre sélection est dans une ville.");
                        return true;
                    }

                    Project check = manager.getZoneHere(block.getLocation());
                    if (check != null && !check.getPlotName().equals(getPlotName())) {
                        player.sendMessage(ChatColor.RED + "Une partie de votre sélection fait partie d'un autre projet");
                        return true;
                    }

                    return false;
                });

        if (cancel)
            return false;

        setArea(area);
        return true;
    }

    public void save() {
        RPMachine.getInstance().getProjectsManager().saveZone(this);
    }

    @Override
    public double getBalance() {
        return money;
    }

    @Override
    public void setBalance(double amount) {
        this.money = amount;
        save();
    }

    @Override
    public boolean withdrawMoney(double amount) {
        if (getBalance() >= amount) {
            setBalance(getBalance() - amount);
            return true;
        }
        return false;
    }

    @Override
    public void creditMoney(double amount) {
        setBalance(getBalance() + amount);
    }

    public Map<String, Date> getLastPaidTaxes() {
        return lastPaidTaxes;
    }

    public void setLastPaidTaxes(Map<String, Date> lastPaidTaxes) {
        this.lastPaidTaxes = lastPaidTaxes;
    }

    @Override
    public void setUnpaidTaxes(String city, double amount) {
        unpaidTaxes.put(city, amount);
        save();
    }

    @Override
    public double getUnpaidTaxes(String city) {
        return unpaidTaxes.get(city);
    }

    @Override
    public void setLastTaxes(String city, Date date) {
        lastPaidTaxes.put(city, date);
    }

    @Override
    public Date getLastTaxes(String city) {
        return lastPaidTaxes.get(city);
    }

    @Override
    public Map<String, Double> getUnpaidTaxes() {
        return unpaidTaxes;
    }

    @Override
    public boolean canActAs(Player p) {
        return canActAsProject(p);
    }

    public void setUnpaidTaxes(Map<String, Double> unpaidTaxes) {
        this.unpaidTaxes = unpaidTaxes;
    }

    @Override
    public boolean hasDelegatedPermission(Player player, DelegatedPermission permission) {
        // Permission d'agir en tant que projet donnée par l'entité possédant le projet
        if (owner().hasDelegatedPermission(player, ProjectPermissions.ACT_AS_PROJECT))
            return true;

            // Permission d'agir en tant que projet donnée au niveau du projet
        else if (admins.containsKey(player.getUniqueId()))
            return admins.get(player.getUniqueId()).contains(permission);

        return false;
    }

    public boolean hasPermission(Player player, ProjectPermissions permission) {
        // Permission directement autorisée parce que le joueur fait partie du propriétaire et a des droits délégués
        if (owner().hasDelegatedPermission(player, permission))
            return true;

            // Permission autorisée au niveau du projet
        else if (admins.containsKey(player.getUniqueId()))
            return admins.get(player.getUniqueId()).contains(permission);
        return false;
    }

    boolean hasDirectPermission(UUID player, Permission permission) {
        return admins.containsKey(player) && admins.get(player).contains(permission);
    }


    void addPermission(UUID target, Permission permission) {
        if (!admins.containsKey(target))
            admins.put(target, new HashSet<>());
        admins.get(target).add(permission);
        save();
    }

    void removePermission(UUID target, Permission permission) {
        if (admins.containsKey(target)) {
            admins.get(target).remove(permission);
            save();
        }
    }

    @Override
    public String displayable() {
        return ChatColor.GREEN + "(Projet) " + ChatColor.DARK_GREEN + getPlotName();
    }

    @Override
    public String shortDisplayable() {
        return ChatColor.DARK_GREEN + getPlotName();
    }

    @Override
    public Set<UUID> getAdministrators() {
        Set<UUID> admins = new HashSet<>(this.admins.keySet());
        admins.addAll(owner().getAdministrators());
        return admins;
    }

    public boolean canActAsProject(Player player) {
        // Permission donnée par le propriétaire de manager ses projets
        if (owner().hasDelegatedPermission(player, ProjectPermissions.ACT_AS_PROJECT))
            return true;

            // Joueur admin du projet
        else if (admins.containsKey(player.getUniqueId()))
            return true;

        return false;
    }

    @Override
    public boolean canBuild(Player player) {
        return getPlotMembers().contains(player.getUniqueId()) ||
                (ownerTag() != null && owner() != null && owner().hasDelegatedPermission(player, ProjectPermissions.BUILD_ON_PROJECT));
    }

    @Override
    protected DelegatedPermission buildPermission() {
        return ProjectPermissions.BUILD_ON_PROJECT;
    }

    @Override
    protected DelegatedPermission interactPermission() {
        return ProjectPermissions.INTERACT_ON_PROJECT;
    }

    public void removeAdmin(UUID id) {
        admins.remove(id);
    }
}
