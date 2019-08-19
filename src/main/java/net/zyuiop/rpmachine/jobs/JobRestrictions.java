package net.zyuiop.rpmachine.jobs;

import net.zyuiop.rpmachine.jobs.restrictions.*;

/**
 * @author Louis Vialar
 */
public enum JobRestrictions {
    ENCHANTING(EnchantingRestriction.class),
    REPAIRING(RepairingRestriction.class),
    FARMING(FarmingRestriction.class),
    KILLING_ANIMALS(KillingRestriction.class),
    CAPTURING(CapturingRestriction.class),
    BREEDING(BreedingRestriction.class);

    private final Class<? extends JobRestriction> restrictionClass;

    JobRestrictions(Class<? extends JobRestriction> restrictionClass) {
        this.restrictionClass = restrictionClass;
    }

    public static JobRestrictions byClass(JobRestriction restriction) {
        for (JobRestrictions r : values())
            if (r.restrictionClass.isInstance(restriction))
                return r;
        throw new NoClassDefFoundError();
    }

    public JobRestriction newInstance() {
        try {
            return restrictionClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
