package net.zyuiop.rpmachine.permissions;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.MenuItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Louis Vialar
 */
public abstract class PermissionsGui extends Window  {
    private final Map<Integer, PermissionTypeIcon> subMenus = new HashMap<>();

    protected final UUID target;
    protected final String targetName;

    public PermissionsGui(Player player, UUID target, String targetName) {
        super(9, "Permissions de " + targetName, player);

        this.target = target;
        this.targetName = targetName;
    }

    protected void addSubMenu(int position, PermissionTypes types, ItemStack itemStack, String name, String... desc) {
        subMenus.put(position, new PermissionTypeIcon(types, name, itemStack, desc));
    }

    protected abstract boolean hasPermission(Permission permission);

    protected abstract void setOrUnsetPermission(Permission permission);

    @Override
    public void fill() {
        for (Map.Entry<Integer, PermissionTypeIcon> sm : subMenus.entrySet()) {
            setItem(sm.getKey(), sm.getValue().menuItem(), () -> openInventory(sm.getValue().types));
        }

        setItem(8, new MenuItem(Material.BARRIER).setName("Fermer"), this::close);
    }

    private void openInventory(PermissionTypes types) {
        new PermissionTypeMenu(types, player).open();
    }

    private class PermissionTypeMenu extends Window {
        private final PermissionTypes types;

        protected PermissionTypeMenu(PermissionTypes types, Player player) {
            super((int) (Math.ceil((types.members().length + 3) / 9D) * 9D), "Permissions de " + targetName + " > " + types.name(), player);

            this.types = types;
        }

        private void setPermItem(int position, Permission permission) {
            boolean has = hasPermission(permission);
            MenuItem stack = new MenuItem(has ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);

            setItem(position, stack.setName(StringUtils.capitalize(permission.description())), () -> {
                RPMachine.getInstance().getLogger().info("Changing permission " + permission + " for " + targetName + " to " + (!has));
                setOrUnsetPermission(permission);
                setPermItem(position, permission); // Reset the item
            });
        }

        @Override
        public void fill() {
            int i = 0;
            for (Permission permission : types.members()) {
                setPermItem(i++, permission);
            }

            setItem(size - 1, new MenuItem(Material.ARROW).setName("Retour"), PermissionsGui.this::open);
        }
    }

    private static final class PermissionTypeIcon {
        private PermissionTypes types;
        private String typeName;
        private String[] typeDescription;
        private ItemStack typeItem;

        public PermissionTypeIcon(PermissionTypes types, String typeName, ItemStack typeItem, String... typeDescription) {
            this.types = types;
            this.typeName = typeName;
            this.typeDescription = typeDescription;
            this.typeItem = typeItem;
        }

        public MenuItem menuItem() {
            return new MenuItem(typeItem).setName(typeName).setDescription(typeDescription);
        }
    }

}
