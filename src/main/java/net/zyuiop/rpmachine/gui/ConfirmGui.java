package net.zyuiop.rpmachine.gui;

import net.zyuiop.rpmachine.utils.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public abstract class ConfirmGui extends Window {
    protected ConfirmGui(String name, Player player) {
        super(5 * 9, name, player);
    }

    protected MenuItem createConfirmItem() {
        return new MenuItem(Material.SLIME_BALL).setName(ChatColor.GREEN + "Accepter");
    }

    protected MenuItem createCancelItem() {
        return new MenuItem(Material.BARRIER).setName(ChatColor.RED + "Refuser");
    }

    protected abstract MenuItem createInfoItem();

    protected abstract void finish(boolean accepted);

    @Override
    public void cancel() {
        finish(false);
    }

    @Override
    public void fill() {
        setItem(3, 2, createConfirmItem(), () -> {
            close(false);
            finish(true);
        });

        setItem(3, 6, createCancelItem(), () -> {
            close(false);
            finish(false);
        });

        setItem(1, 4, createInfoItem(), () -> {
        });
    }

}
