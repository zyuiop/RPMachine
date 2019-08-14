package net.zyuiop.rpmachine.projects;

import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.permissions.Permission;
import net.zyuiop.rpmachine.permissions.PermissionTypes;
import net.zyuiop.rpmachine.permissions.PermissionsGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.function.Predicate;

/**
 * @author Louis Vialar
 */
public class ProjectsPermGui extends PermissionsGui {
    private final Project project;

    public ProjectsPermGui(Player player, UUID target, String tName, Project project) {
        super(player, target, tName);

        this.project = project;

        addSubMenu(0, PermissionTypes.ECONOMY, new ItemStack(Material.GOLD_BLOCK), "Economie", "Régule les transactions que peut", "effectuer le joueur avec le compte", "bancaire du projet");
        addSubMenu(1, PermissionTypes.PLOTS, new ItemStack(Material.DIRT), "Parcelles", "Régule les actions que peut", "entreprendre le joueur sur les", "parcelles possédées par le projet");
        addSubMenu(2, PermissionTypes.PROJECTS, new ItemStack(Material.ACACIA_FENCE), "Projets", "Régule les actions que peut", "entreprendre le joueur sur les", "projets possédés par le projet (ça existe)");
        addSubMenu(3, PermissionTypes.SHOPS, new ItemStack(Material.BIRCH_SIGN), "Boutiques", "Régule les actions que peut", "entreprendre le joueur sur les", "boutiques possédées par le projet");
    }

    @Override
    protected boolean hasPermission(Permission permission) {
        return project.hasDirectPermission(target, permission);
    }

    @Override
    protected void setOrUnsetPermission(Permission permission) {
        if (!hasPermission(permission)) {
            project.addPermission(target, permission);
        } else {
            project.removePermission(target, permission);
        }
    }
}
