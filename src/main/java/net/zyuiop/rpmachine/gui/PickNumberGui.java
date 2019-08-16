package net.zyuiop.rpmachine.gui;

import net.zyuiop.rpmachine.utils.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public abstract class PickNumberGui extends Window {
    protected double value;
    protected double[] steps;

    protected double min = Double.NEGATIVE_INFINITY;
    protected double max = Double.POSITIVE_INFINITY;

    protected PickNumberGui(String name, Player player, double step1, double step2, double step3, double value) {
        super(27, name, player);

        this.steps = new double[] { step1, step2, step3 };
        this.value = value;
    }

    public abstract MenuItem updateItem(double value);

    private void updateCentralItem() {
        setItem(4, updateItem(value), () -> {
            close(false);
            finish(value);
        });
    }

    protected abstract void finish(double value);

    @Override
    public void fill() {
        for (int i = 0; i < 3; ++i) {
            final int fI = i;
            setItem(i, new MenuItem(Material.RED_STAINED_GLASS_PANE).setName(ChatColor.RED + " - " + steps[i]), () -> updateValue(-steps[fI]));
            setItem(8 - i, new MenuItem(Material.GREEN_STAINED_GLASS_PANE).setName(ChatColor.GREEN + " + " + steps[i]), () -> updateValue(steps[fI]));
        }
        updateCentralItem();

        setItem(2, 8, new MenuItem(Material.SLIME_BALL).setName(ChatColor.GREEN + "Valider"), () -> {
            close(false);
            finish(value);
        });
    }

    private void updateValue(double diff) {
        if (value + diff > max || value + diff < min)
            return;

        value += diff;
        updateCentralItem();
    }
}
