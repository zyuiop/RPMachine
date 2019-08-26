package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.auctions.Order;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.MenuItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Louis Vialar
 */
public abstract class AbstractOrderListGui<T extends Order> extends Window {
    private List<T> orders = new ArrayList<>();
    private Window backTo;
    protected final Material mat;

    AbstractOrderListGui(String title, Window backTo, Player player, Material mat) {
        super(9 * 6, title, player);
        this.backTo = backTo;
        this.mat = mat;

        orders.addAll(getOrders());
    }

    @Override
    public final void fill() {
        setItem(size - 1, new MenuItem(Material.ARROW).setName(ChatColor.YELLOW + "Retour"), () -> {
            close();
            backTo.open();
        });

        setItem(size - 2, new MenuItem(Material.WATER_BUCKET).setName(ChatColor.AQUA + "Raffraichir"), this::refresh);

        load();
    }

    protected abstract Collection<T> getOrders();

    protected abstract MenuItem getItem(T order);

    protected abstract Runnable getAction(T order);

    protected void refresh() {
        orders.clear();
        orders.addAll(getOrders());

        clear();

        fill();
    }

    private void load() {
        for (int i = 0; i < (orders.size()) && i < size - 3; ++i) {
            T order = orders.get(i);

            setItem(i, getItem(order), getAction(order));
        }
    }
}
