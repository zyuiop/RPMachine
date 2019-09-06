package net.zyuiop.rpmachine.claims;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Louis Vialar
 */
public abstract class LeafClaim implements Claim {
    @Override
    public Collection<Claim> getClaims() {
        return Collections.emptySet();
    }
}
