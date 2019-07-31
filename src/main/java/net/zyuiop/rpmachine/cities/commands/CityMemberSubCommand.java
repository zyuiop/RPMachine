package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.commands.SubCommand;
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

        return true;
    }

    @Override
    default boolean run(Player player, String[] args) {
        City city = RPMachine.getInstance().getCitiesManager().getPlayerCity(player.getUniqueId());
        return run(player, city, args);
    }

    default boolean requiresCouncilPrivilege() {
        return false;
    }

    default boolean requiresMayorPrivilege() {
        return false;
    }

    boolean run(Player player, @Nonnull City city, String[] args);
}
