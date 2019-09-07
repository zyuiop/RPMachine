package net.zyuiop.rpmachine.claims;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Louis Vialar
 */
public final class DenyAllClaim implements Claim {
    public static final Claim INSTANCE = new DenyAllClaim();

    private DenyAllClaim() {}


    @Override
    public boolean isInside(Location location) {
        return false;
    }

    @Override
    public boolean canBuild(Player player, Location location) {
        return false;
    }

    @Override
    public boolean canInteractWithBlock(Player player, Block block, Action action) {
        return false;
    }

    @Override
    public boolean canInteractWithEntity(Player player, Entity entity) {
        return false;
    }

    @Override
    public boolean canDamageEntity(Player player, Entity entity) {
        return false;
    }

    @Override
    public Collection<Claim> getClaims() {
        return Collections.emptySet();
    }
}
