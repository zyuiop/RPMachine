package net.zyuiop.rpmachine.entities;

import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * @author zyuiop
 */
public class RoleToken {
    private final Player player;
    private final LegalEntity controlledEntity;

    public RoleToken(Player player, LegalEntity controlledEntity) {
        this.player = player;
        this.controlledEntity = controlledEntity;
    }

    public LegalEntity getLegalEntity() {
        return controlledEntity;
    }

    public Player getPlayer() {
        return player;
    }

    public String getTag() {
        return controlledEntity.tag();
    }

    /**
     * Check if the given player has the right to execute the given command as the current taxpayer
     *
     * @param permission the permission to check
     * @return true if the command is allowed, false if not
     */
    public boolean hasDelegatedPermission(@Nonnull DelegatedPermission permission) {
        Validate.notNull(permission);

        return getLegalEntity().hasDelegatedPermission(player, permission);
    }

    public boolean checkDelegatedPermission(@Nonnull DelegatedPermission permission) {
        if (!hasDelegatedPermission(permission)) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de faire ça en tant que " + controlledEntity.displayable());
            return false;
        }

        return true;
    }
}
