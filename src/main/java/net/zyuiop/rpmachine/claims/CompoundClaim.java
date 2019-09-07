package net.zyuiop.rpmachine.claims;

import net.zyuiop.rpmachine.json.JsonExclude;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

/**
 * A claim that contains zero, one, or more claims underneath it.
 * @author Louis Vialar
 */
public abstract class CompoundClaim implements Claim {
    public Claim getClaimOrDefault(Location location) {
        return getClaimAt(location).orElse(DenyAllClaim.INSTANCE);
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        return getClaimOrDefault(location).canBuild(player, location);
    }

    @Override
    public boolean canInteractWithBlock(Player player, Block block, Action action) {
        return getClaimOrDefault(block.getLocation()).canInteractWithBlock(player, block, action);
    }

    @Override
    public boolean canInteractWithEntity(Player player, Entity entity) {
        return getClaimOrDefault(entity.getLocation()).canInteractWithEntity(player, entity);
    }

    @Override
    public boolean canDamageEntity(Player player, Entity entity) {
        return getClaimOrDefault(entity.getLocation()).canDamageEntity(player, entity);
    }
}
