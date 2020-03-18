package net.zyuiop.rpmachine.cities.politics;

import net.zyuiop.rpmachine.permissions.Permission;

import java.util.Set;

/**
 * @author Louis Vialar
 */
public class Tyrany implements PoliticalSystem {
    public static Tyrany INSTANCE = new Tyrany();
    private final Set<Permission> DISABLED_PERMS = Set.of();

    private Tyrany() {
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
}
