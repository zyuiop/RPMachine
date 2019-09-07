package net.zyuiop.rpmachine.claims;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

/**
 * @author Louis Vialar
 */
public interface Claim extends ClaimCollectionRegistry {

    /**
     * Checks that a given location is inside the claim
     *
     * @param location the location to check
     * @return true if the location is in the claim, false if it is not
     */
    boolean isInside(Location location);

    /**
     * Checks if a given player can build at a location inside the claim.
     * If the location is not inside the claim, the behaviour is undefined.
     *
     * @param player   the player trying to build
     * @param location the location where the player is trying to build
     * @return true if the player can build there
     */
    boolean canBuild(Player player, Location location);

    /**
     * Checks if a given player can interact with a given block. It is assumed that the block is inside the claim,
     * behaviour is undefined if it is not.
     *
     * @param player the player trying to interact
     * @param block  the block the player tries to interact with
     * @param action the interact action
     * @return true if the player is allowed to interact with that block
     */
    boolean canInteractWithBlock(Player player, Block block, Action action);

    /**
     * Checks if a given player can interact with a given entity. It is assumed that the entity location is inside the
     * claim, behaviour is undefined if it is not.
     *
     * @param player the player trying to interact
     * @param entity the entity the player is trying to interact with
     * @return true if the player is allowed to interact with that entity
     */
    boolean canInteractWithEntity(Player player, Entity entity);

    /**
     * Checks if a given player can damage a given entity. It is assumed that the entity location is inside the
     * claim, behaviour is undefined if it is not.
     *
     * @param player the player trying to interact
     * @param entity the entity the player is trying to interact with
     * @return true if the player is allowed to interact with that entity
     */
    boolean canDamageEntity(Player player, Entity entity);

}
