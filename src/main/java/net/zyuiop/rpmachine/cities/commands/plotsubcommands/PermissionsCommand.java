package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.cities.CityPlot;
import net.zyuiop.rpmachine.cities.commands.PlotCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.PlotPermissions;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PermissionsCommand implements SubCommand {
    private final CitiesManager citiesManager;

    public PermissionsCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<ville> <parcelle> <everyone|citizens>";
    }

    @Override
    public String getDescription() {
        return "change les permissions des externes sur votre parcelle";
    }

    @Override
    public boolean canUse(Player player) {
        RoleToken tt = RPMachine.getPlayerRoleToken(player);

        return tt.hasDelegatedPermission(PlotPermissions.CHANGE_PUBLIC_PERMISSIONS);
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {
        var tuple = PlotCommand.getTupleAndArgs(args, player);

        if (tuple == null || tuple.getT3().length < 1) {
            player.sendMessage(ChatColor.RED + "Utilisation : /" + command + " " + subCommand + " " + getUsage());
            return false;
        }

        args = tuple.getT3();
        var plot = tuple.getT2();
        var city = tuple.getT1();

        RoleToken tt = RPMachine.getPlayerRoleToken(player);
        if (!plot.ownerTag().equals(tt.getTag()) && !tt.hasDelegatedPermission(PlotPermissions.CHANGE_PUBLIC_PERMISSIONS)) {
            player.sendMessage(ChatColor.RED + "Cette parcelle ne vous appartient pas.");
        } else {
            if (args[0].equalsIgnoreCase("everyone")) {
                player.sendMessage(ChatColor.GREEN + "Modification des permissions de tous les joueurs...");
                plot.externalPermissionsGui(player, city::save).open();
            } else if (args[0].equalsIgnoreCase("citizens")) {
                player.sendMessage(ChatColor.GREEN + "Modification des permissions de tous les citoyens de la ville...");
                plot.citizensPermissionsGui(player, city::save).open();
            } else {
                player.sendMessage(ChatColor.RED + "Argument incorrect, utilisez citizens (pour modifier les permissions des citoyens de la ville) ou everyone (pour modifier les permissions de tous les joueurs).");
            }
        }
        return true;
    }
}
