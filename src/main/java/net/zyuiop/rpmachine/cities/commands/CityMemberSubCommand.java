package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * @author Louis Vialar
 */
public interface CityMemberSubCommand extends SubCommand {
    @Override
    default boolean canUse(Player player) {
        City city = RPMachine.getInstance().getCitiesManager().getPlayerCity(player.getUniqueId());

        if (city == null)
            return false;

        if (requiresMayorPrivilege())
            return city.getMayor().equals(player.getUniqueId());

        if (requiresCouncilPrivilege())
            return city.getCouncils().contains(player.getUniqueId()) || city.getMayor().equals(player.getUniqueId());

        if (requiresPermission() != null)
            return city.hasPermission(player, requiresPermission());

        return true;
    }

    @Override
    default boolean run(Player player, String command, String subCommand, String[] args) {
        City city = RPMachine.getInstance().getCitiesManager().getPlayerCity(player.getUniqueId());
        return run(player, city, command, subCommand, args);
    }

    default Permission requiresPermission() {
        return null;
    }

    default boolean requiresCouncilPrivilege() {
        return false;
    }

    default boolean requiresMayorPrivilege() {
        return false;
    }

    boolean run(Player player, @Nonnull City city, String command, String subCommand, String[] args);
}
