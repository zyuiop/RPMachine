package net.zyuiop.rpmachine.permissions;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.MenuItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public class PlotPermissionsGui extends Window {
    private final Plot plot;
    private final Runnable save;

    public PlotPermissionsGui(Player player, Plot plot, Runnable save) {
        super(9, "Permissions sur " + plot.getPlotName(), player);
        this.plot = plot;
        this.save = save;
    }

    protected boolean hasPermission(Plot.ExternalPlotPermissions permission) {
        return plot.canExternalDo(permission);
    }

    protected void togglePermission(Plot.ExternalPlotPermissions permission) {
        plot.setExternalPermission(permission);
    }

    private void setPermItem(int position, Plot.ExternalPlotPermissions permission) {
        boolean has = hasPermission(permission);
        MenuItem stack = new MenuItem(has ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);

        setItem(position, stack.setName(StringUtils.capitalize(permission.description())), () -> {
            RPMachine.getInstance().getLogger().info("Changing plot permission " + permission + " to " + (!has));
            togglePermission(permission);
            setPermItem(position, permission); // Reset the item
        });
    }

    @Override
    public void fill() {
        int i = 0;
        for (Plot.ExternalPlotPermissions permission : Plot.ExternalPlotPermissions.values()) {
            setPermItem(i++, permission);
        }

        setItem(size - 1, new MenuItem(Material.ARROW).setName("Fermer"), this::close);
    }

    @Override
    public void close() {
        this.save.run();
        super.close();
    }
}
