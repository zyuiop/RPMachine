package net.zyuiop.rpmachine.cities.politics;

import com.google.common.collect.ImmutableSet;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;

import java.util.Set;

/**
 * @author Louis Vialar
 */
public class StateOfRights implements PoliticalSystem {
    public static StateOfRights INSTANCE = new StateOfRights();

    private StateOfRights() {}

    @Override
    public String getDescription() {
        return "Une ville dans lequel les droits des citoyens sont protégés.";
    }

    @Override
    public boolean allowInstantPlotDelete() {
        return false;
    }

    private final Set<Permission> DISABLED_PERMS = ImmutableSet.of(CityPermissions.BUILD_IN_PLOTS, CityPermissions.INTERACT_IN_PLOTS, CityPermissions.REDEFINE_OCCUPIED_PLOT);

    @Override
    public Set<Permission> disabledPerms() {
        return DISABLED_PERMS;
    }

    @Override
    public Set<String> protectedParameters() {
        return Set.of("politicalSystem")
    }
}
