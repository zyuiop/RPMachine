package net.zyuiop.rpmachine.cities.politics;

import net.zyuiop.rpmachine.permissions.Permission;

import java.util.Set;

/**
 * @author Louis Vialar
 */
public interface PoliticalSystem {
    String getDescription();

    boolean allowInstantPlotDelete();

    Set<Permission> disabledPerms();

    default boolean isRestricted(Permission perm) {
        return disabledPerms().contains(perm);
    }

}
