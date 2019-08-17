package net.zyuiop.rpmachine.gui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.utils.MenuItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Louis Vialar
 */
public abstract class Window {
    static final String METADATA_KEY = "openedWindow";

    private final Map<Integer, Runnable> actions = new HashMap<>();
    protected final int size;
    protected final String name;
    protected final Player player;
    Inventory inventory;

    protected Window(int size, String name, Player player) {
        this.size = size;
        this.name = name;
        this.player = player;
    }

    public abstract void fill();

    public static Window createWindow(Class<? extends Window> window, Player player) throws ReflectiveOperationException {
        return window.getConstructor(Player.class).newInstance(player);
    }

    void clickPosition(int pos) {
        if (actions.containsKey(pos)) {
            actions.get(pos).run();
        }
    }

    public void open() {
        closeOtherInventories();

        inventory = Bukkit.createInventory(player, size, name);
        fill();
        player.openInventory(inventory);
        player.setMetadata(METADATA_KEY, new FixedMetadataValue(RPMachine.getInstance(), this));
    }

    protected void setItem(int position, ItemStack item, Runnable onClick) {
        actions.put(position, onClick);
        inventory.setItem(position, item);
    }

    protected void clear() {
        actions.clear();
        inventory.clear();
    }

    protected void setItem(int row, int col, ItemStack item, Runnable onClick) {
        setItem(row * 9 + col, item, onClick);
    }

    protected void setItem(int position, MenuItem item, Runnable onClick) {
        setItem(position, item.build(), onClick);
    }

    protected void setItem(int row, int col, MenuItem item, Runnable onClick) {
        setItem(row * 9 + col, item, onClick);
    }

    private void closeOtherInventories() {
        Window other = getOpenWindow(player);
        if (other != null)
            other.close();
    }

    static Window getOpenWindow(HumanEntity player) {
        for (MetadataValue value : player.getMetadata(Window.METADATA_KEY))
            if (value.getOwningPlugin().equals(RPMachine.getInstance()))
                return (Window) value.value();
        return null;
    }

    private boolean closing = false;

    public void cancel() {

    }

    public void close() {
        close(true);
    }

    public void close(boolean isCancel) {
        if (closing) // Prevent inf loops
            return;

        closing = true;
        if (player.getOpenInventory().getTopInventory() == inventory) {
            player.closeInventory();

            if (isCancel)
                cancel();
        }
        closing = false;

        inventory = null;

        player.removeMetadata(METADATA_KEY, RPMachine.getInstance());
    }

}
