package net.zyuiop.rpmachine.entities;

import javax.annotation.Nullable;

/**
 * @author Louis Vialar
 */
public interface Ownable {
    /**
     * Get the legal entity tag of the person who owns the current object
     */
    @Nullable String ownerTag();

    default LegalEntity owner() {
        return LegalEntity.getEntity(ownerTag());
    }
}
