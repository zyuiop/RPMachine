package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.commands.SubCommand;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.permissions.PlotPermissions;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LeaveCommand implements SubCommand {
    private final CitiesManager citiesManager;

    public LeaveCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<ville> <parcelle>";
    }

    @Override
    public String getDescription() {
        return "quitte la parcelle choisie";
    }

    @Override
    public boolean canUse(Player player) {
        return RPMachine.getPlayerRoleToken(player).checkDelegatedPermission(PlotPermissions.LEAVE_PLOT);
    }

    @Override
    public boolean run(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Utilisation : /plot leave " + getUsage());
            return true;
        }

        City city = citiesManager.getCity(args[0]);
        if (city == null) {
            player.sendMessage(ChatColor.RED + "Cette ville n'existe pas.");
            return true;
        }

        Plot plot = city.getPlots().get(args[1]);
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "Cette parcelle n'existe pas.");
            return true;
        } else if (!plot.getOwner().equals(RPMachine.getPlayerRoleToken(player))) {
            player.sendMessage(ChatColor.RED + "Cette parcelle ne vous appartient pas.");
            return true;
        } else {
            plot.setOwner((String) null);
            citiesManager.saveCity(city);
            player.sendMessage(ChatColor.GREEN + "Vous n'êtes plus propriétaire de cette parcelle.");
            return true;
        }
    }
}
