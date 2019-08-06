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
import java.util.function.Predicate;

/**
 * @author Louis Vialar
 */
public abstract class PermissionsGui extends Window {
    private final Map<Integer, PermissionTypeIcon> subMenus = new HashMap<>();

    protected final UUID target;
    protected final String targetName;

    public PermissionsGui(Player player, UUID target, String targetName) {
        super(9, "Permissions de " + targetName, player);

        this.target = target;
        this.targetName = targetName;
    }

    protected void addSubMenu(int position, PermissionTypes types, ItemStack itemStack, String name, String... desc) {
        addSubMenu(position, types, itemStack, permission -> true, name, desc);
    }

    protected void addSubMenu(int position, PermissionTypes types, ItemStack itemStack, Predicate<Permission> filter, String name, String... desc) {
        subMenus.put(position, new PermissionTypeIcon(types, name, itemStack, filter, desc));
    }


    protected abstract boolean hasPermission(Permission permission);

    protected abstract void setOrUnsetPermission(Permission permission);

    @Override
    public void fill() {
        for (Map.Entry<Integer, PermissionTypeIcon> sm : subMenus.entrySet()) {
            setItem(sm.getKey(), sm.getValue().menuItem(), () -> openInventory(sm.getValue().types, sm.getValue().filter));
        }

        setItem(8, new MenuItem(Material.BARRIER).setName("Fermer"), this::close);
    }

    private void openInventory(PermissionTypes types, Predicate<Permission> filter) {
        new PermissionTypeMenu(types, player, filter).open();
    }

    private class PermissionTypeMenu extends Window {
        private final PermissionTypes types;
        private final Predicate<Permission> filter;

        protected PermissionTypeMenu(PermissionTypes types, Player player, Predicate<Permission> filter) {
            super((int) (Math.ceil((types.members().length + 3) / 9D) * 9D), "Permissions de " + targetName + " > " + types.name(), player);

            this.types = types;
            this.filter = filter;
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
                if (filter.test(permission))
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
        private Predicate<Permission> filter;

        public PermissionTypeIcon(PermissionTypes types, String typeName, ItemStack typeItem, Predicate<Permission> filter, String... typeDescription) {
            this.types = types;
            this.typeName = typeName;
            this.typeDescription = typeDescription;
            this.typeItem = typeItem;
            this.filter = filter;
        }

        public MenuItem menuItem() {
            return new MenuItem(typeItem).setName(typeName).setDescription(typeDescription);
        }
    }

}
