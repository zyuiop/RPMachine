package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.PlotPermissions;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MembersCommand implements SubCommand {
    private final CitiesManager citiesManager;

    public MembersCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<ville> <parcelle> <add|remove> <joueur>";
    }

    @Override
    public String getDescription() {
        return "ajoute ou supprime un membre de votre parcelle";
    }

    @Override
    public boolean canUse(Player player) {
        RoleToken tt = RPMachine.getPlayerRoleToken(player);

        return tt.hasDelegatedPermission(PlotPermissions.ADD_NEW_MEMBER) ||
                tt.hasDelegatedPermission(PlotPermissions.REMOVE_MEMBER) || (
                        citiesManager.getPlayerCity(player) != null && citiesManager.getPlayerCity(player).hasPermission(player, CityPermissions.CHANGE_PLOT_MEMBERS));
    }

    @Override
    public boolean run(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Utilisation : /plot members " + getUsage());
            return false;
        }

        City city = citiesManager.getCity(args[0]);
        if (city == null) {
            player.sendMessage(ChatColor.RED + "Cette ville n'existe pas.");
            return true;
        }

        Plot plot = city.getPlots().get(args[1]);
        RoleToken tt = RPMachine.getPlayerRoleToken(player);
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "Cette parcelle n'existe pas.");
        } else if (!city.hasPermission(player, CityPermissions.CHANGE_PLOT_MEMBERS) && !plot.getOwner().equals(tt)) {
            player.sendMessage(ChatColor.RED + "Cette parcelle ne vous appartient pas.");
        } else {
            UUID id = RPMachine.database().getUUIDTranslator().getUUID(args[3], true);
            if (id == null) {
                player.sendMessage(ChatColor.RED + "Ce joueur n'a pas été trouvé.");
            } else {
                if (args[2].equalsIgnoreCase("add") && (city.hasPermission(player, CityPermissions.CHANGE_PLOT_MEMBERS) || tt.checkDelegatedPermission(PlotPermissions.ADD_NEW_MEMBER))) {
                    if (plot.getPlotMembers().contains(id)) {
                        player.sendMessage(ChatColor.GREEN + "Ce joueur est déjà dans la parcelle.");
                        return true;
                    }
                    plot.getPlotMembers().add(id);
                    citiesManager.saveCity(city);
                    player.sendMessage(ChatColor.GREEN + "Le joueur a été ajouté dans la parcelle.");
                } else if (args[2].equalsIgnoreCase("remove") && (city.hasPermission(player, CityPermissions.CHANGE_PLOT_MEMBERS) || tt.checkDelegatedPermission(PlotPermissions.REMOVE_MEMBER))) {
                    if (!plot.getPlotMembers().contains(id)) {
                        player.sendMessage(ChatColor.GREEN + "Ce joueur n'est pas dans la parcelle.");
                        return true;
                    }
                    plot.getPlotMembers().remove(id);
                    citiesManager.saveCity(city);
                    player.sendMessage(ChatColor.GREEN + "Le joueur a été supprimé de la parcelle.");
                } else {
                    player.sendMessage(ChatColor.RED + "Argument invalide (add / remove)");
                }
            }
        }
        return true;
    }
}
