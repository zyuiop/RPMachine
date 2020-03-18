package net.zyuiop.rpmachine.cities.politics;

import net.zyuiop.rpmachine.permissions.Permission;

import java.util.Set;

/**
 * @author Louis Vialar
 */
public class Tyranny implements PoliticalSystem {
    public static Tyranny INSTANCE = new Tyranny();
    private final Set<Permission> DISABLED_PERMS = Set.of();

    private Tyranny() {
    }

    @Override
    public String getName() {
        return "Tyrannie";
    }

    @Override
    public String getDescription() {
        return "Une ville dans laquelle le maire et le conseil municipal ont un pouvoir absolu.";
    }

    @Override
    public boolean allowInstantPlotDelete() {
        return true;
    }

    @Override
    public Set<Permission> disabledPerms() {
        return DISABLED_PERMS;
    }

    @Override
    public Set<String> protectedParameters() {
        return Set.of();
    }
}
