package net.zyuiop.rpmachine.claims;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Louis Vialar
 */
public interface ClaimRegistry {
    /**
     * Gets the claim at a given location
     *
     * @param loc the location to check
     * @return an optional claim, depending on whether there is or not a claim there
     */
    Optional<Claim> getClaimAt(org.bukkit.Location loc);

    default List<Claim> getClaimTreeAt(Location loc) {
        Optional<Claim> current = getClaimAt(loc);
        List<Claim> tree = new ArrayList<>();

        while (current.isPresent()) {
            tree.add(current.get());
            current = current.get().getClaimAt(loc);
        }

        return tree;
    }

    /**
     * Get, at the given location, the last claim of the tree, i.e. the only one that has no other child
     * @param loc the location
     * @return an optional, empty if there is no claim here
     */
    default Optional<Claim> getLowestLevelClaimAt(Location loc) {
        List<Claim> tree = getClaimTreeAt(loc);

        return tree.isEmpty() ? Optional.empty() : Optional.of(tree.get(tree.size() - 1));
    }
}
