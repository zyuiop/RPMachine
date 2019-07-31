package net.zyuiop.rpmachine.permissions;

/**
 * @author Louis Vialar
 */
public interface PermissionHolder {
    boolean hasPermission(DelegatedPermission permission);
}
