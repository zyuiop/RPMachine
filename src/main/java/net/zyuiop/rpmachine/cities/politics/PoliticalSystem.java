package net.zyuiop.rpmachine.cities.politics;

import net.zyuiop.rpmachine.permissions.Permission;

import java.util.Set;

/**
 * @author Louis Vialar
 */
public interface PoliticalSystem {
    String getName();
    String getDescription();

    boolean allowInstantPlotDelete();

    Set<Permission> disabledPerms();

    /**
     * Which parameters in the city are protected and require a vote to be changed
     */
    Set<String> protectedParameters();

    default boolean isPermissionRestricted(Permission perm) {
        return disabledPerms().contains(perm);
    }

    default boolean isParameterRestricted(String parameter) {
        return protectedParameters().contains(parameter);
    }

}
