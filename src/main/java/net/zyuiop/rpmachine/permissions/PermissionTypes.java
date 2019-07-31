package net.zyuiop.rpmachine.permissions;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Louis Vialar
 */
public enum PermissionTypes {
    CITY(CityPermissions.class),
    ECONOMY(EconomyPermissions.class),
    PLOTS(PlotPermissions.class),
    PROJECTS(ProjectPermissions.class),
    SHOPS(ShopPermissions.class);

    private final Class<? extends Enum<? extends Permission>> clazz;

    PermissionTypes(Class<? extends Enum<? extends Permission>> clazz) {
        this.clazz = clazz;
    }

    public static PermissionTypes get(Permission permission) throws NoClassDefFoundError {
        for (PermissionTypes v : values())
            if (v.clazz.isInstance(permission))
                return v;

        throw new NoClassDefFoundError(permission.getClass().getName());
    }

    public Permission getPermission(String name) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return (Permission) clazz.getMethod("valueOf", String.class).invoke(null, name);
    }
}
