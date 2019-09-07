package net.zyuiop.rpmachine.claims;

import java.util.Collection;
import java.util.Optional;

/**
 * @author Louis Vialar
 */
public interface ClaimCollectionRegistry extends ClaimRegistry {
    /**
     * Gets the claim at a given location
     *
     * @param loc the location to check
     * @return an optional claim, depending on whether there is or not a claim there
     */
    default Optional<Claim> getClaimAt(org.bukkit.Location loc) {
        return getClaims().parallelStream().filter(c -> c.isInside(loc)).findAny().map(c -> (Claim) c);
    }

    /**
     * Get the claims contained in the current claim
     */
    Collection<? extends Claim> getClaims();
}
