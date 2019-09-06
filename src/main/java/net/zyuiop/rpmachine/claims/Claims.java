package net.zyuiop.rpmachine.claims;

import net.zyuiop.rpmachine.RPMachine;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Louis Vialar
 */
public final class Claims implements ClaimRegistry {
    private final List<ClaimRegistry> registries = new ArrayList<>();

    public Claims(RPMachine machine) {
        registries.add(machine.getCitiesManager());
        registries.add(machine.getProjectsManager());
    }

    @Override
    public Optional<Claim> getClaimAt(Location loc) {
        return registries.stream()
                .map(r -> r.getClaimAt(loc))
                .reduce(Optional.empty(), (o1, o2) -> o1.isPresent() ? o1 : o2);
    }

    public boolean canBuild(Player player, Location location) {
        return getClaimAt(location).map(c -> c.canBuild(player, location)).orElse(true);
    }

    public boolean canInteractWithBlock(Player player, Block block, Action action) {
        return getClaimAt(block.getLocation()).map(c -> c.canInteractWithBlock(player, block, action)).orElse(true);
    }

    public boolean canInteractWithEntity(Player player, Entity entity) {
        return getClaimAt(entity.getLocation()).map(c -> c.canInteractWithEntity(player, entity)).orElse(true);
    }

    public boolean canDamageEntity(Player player, Entity entity) {
        return getClaimAt(entity.getLocation()).map(c -> c.canDamageEntity(player, entity)).orElse(true);
    }

    public boolean isClaimed(Location location) {
        return getClaimAt(location).isPresent();
    }
}
