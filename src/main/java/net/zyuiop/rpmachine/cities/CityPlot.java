package net.zyuiop.rpmachine.cities;

import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.permissions.PlotPermissionsGui;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class CityPlot extends Plot {
    private Set<ExternalPlotPermissions> citizensPermissions = new HashSet<>();

    public boolean canCitizenDo(ExternalPlotPermissions perm) {
        return citizensPermissions.contains(perm);
    }

    public void setCitizenPermission(ExternalPlotPermissions perm) {
        if (citizensPermissions.contains(perm)) citizensPermissions.remove(perm);
        else citizensPermissions.add(perm);
    }

    public Window citizensPermissionsGui(Player player, Runnable save) {
        return new PlotPermissionsGui(player, this, save) {
            @Override
            protected boolean hasPermission(ExternalPlotPermissions permission) {
                return canCitizenDo(permission);
            }

            @Override
            protected void togglePermission(ExternalPlotPermissions permission) {
                setCitizenPermission(permission);
            }
        };
    }

    public boolean canBuild(Player player, boolean isCitizen) {
        return (isCitizen && canCitizenDo(ExternalPlotPermissions.BUILD)) ||
                super.canBuild(player);
    }

    public boolean canInteractWithBlock(Player player, boolean isCitizen) {
        return (isCitizen && canCitizenDo(ExternalPlotPermissions.INTERACT_BLOCKS)) ||
                super.canInteractWithBlock(player);
    }

    public boolean canInteractWithEntity(Player player, boolean isCitizen) {
        return (isCitizen && canCitizenDo(ExternalPlotPermissions.INTERACT_ENTITIES)) ||
                super.canInteractWithEntity(player);
    }
}
