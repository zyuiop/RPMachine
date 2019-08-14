package net.zyuiop.rpmachine.cities;

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
public class CitiesPermGui extends PermissionsGui {
    private final City city;

    public CitiesPermGui(Player player, UUID target, String tName, City city) {
        super(player, target, tName);

        this.city = city;

        Predicate<Permission> filter = p -> !city.getPoliticalSystem().isRestricted(p);

        addSubMenu(0, PermissionTypes.CITY, new ItemStack(Material.BRICK), filter, "Gestion de la ville", "Permissions liées à la gestion", "de la ville en elle même");
        addSubMenu(1, PermissionTypes.ECONOMY, new ItemStack(Material.GOLD_BLOCK), filter, "Economie", "Régule les transactions que peut", "effectuer le joueur avec le compte", "bancaire de la ville");
        addSubMenu(2, PermissionTypes.PLOTS, new ItemStack(Material.DIRT), filter, "Parcelles", "Régule les actions que peut", "entreprendre le joueur sur les", "parcelles possédées par la ville");
        addSubMenu(3, PermissionTypes.PROJECTS, new ItemStack(Material.ACACIA_FENCE), filter, "Projets", "Régule les actions que peut", "entreprendre le joueur sur les", "projets possédés par la ville");
        addSubMenu(4, PermissionTypes.SHOPS, new ItemStack(Material.BIRCH_SIGN), filter, "Boutiques", "Régule les actions que peut", "entreprendre le joueur sur les", "boutiques possédées par la ville");
    }

    @Override
    protected boolean hasPermission(Permission permission) {
        return city.hasPermission(target, permission);
    }

    @Override
    protected void setOrUnsetPermission(Permission permission) {
        if (!hasPermission(permission)) {
            city.addPermission(target, permission);
        } else {
            city.removePermission(target, permission);
        }
    }
}
