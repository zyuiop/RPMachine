package net.zyuiop.rpmachine.projects;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.common.Area;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.database.StoredEntity;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import net.zyuiop.rpmachine.permissions.ProjectPermissions;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author zyuiop
 * A zone is a kind of plot located outside of any city. It can only be created with administrative permissions.<br/>
 * A zone has no taxes as it doesn't depend of any city.
 */
public class Project extends Plot implements LegalEntity, StoredEntity {
    private final Map<UUID, Set<DelegatedPermission>> admins = new HashMap<>();
    private String welcomeMessage;
    private String goodByeMessage;
    private String fileName;
    private double money = 0D;
    private Map<String, Double> unpaidTaxes = new HashMap<>();
    private Map<String, Date> lastPaidTaxes = new HashMap<>();

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public String getGoodByeMessage() {
        return goodByeMessage;
    }

    public void setGoodByeMessage(String goodByeMessage) {
        this.goodByeMessage = goodByeMessage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean checkArea(Area area, ProjectsManager manager, Player player) {
        int i_x = area.getMinX();
        while (i_x < area.getMaxX()) {
            int i_z = area.getMinZ();
            while (i_z < area.getMaxZ()) {
                if (RPMachine.getInstance().getCitiesManager().getCityHere(new Location(Bukkit.getWorld("world"), i_x, 64, i_z).getChunk()) != null) {
                    player.sendMessage(ChatColor.RED + "Une partie de votre sélection est dans une ville.");
                    return false;
                }

                int i_y = area.getMinY();
                while (i_y < area.getMaxY()) {
                    Project check = manager.getZoneHere(new Location(Bukkit.getWorld("world"), i_x, i_y, i_z));
                    if (check != null && !check.getPlotName().equals(getPlotName())) {
                        player.sendMessage(ChatColor.RED + "Une partie de votre sélection fait partie d'un autre projet");
                        return false;
                    }
                    i_y++;
                }
                i_z++;
            }
            i_x++;
        }

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
    public boolean canBuild(Player player, Location location) {
        return getPlotMembers().contains(player.getUniqueId()) ||
                (ownerTag() != null && owner() != null && owner().hasDelegatedPermission(player, ProjectPermissions.BUILD_ON_PROJECT));
    }
}
