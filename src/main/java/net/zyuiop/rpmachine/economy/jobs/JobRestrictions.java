package net.zyuiop.rpmachine.economy.jobs;

import net.zyuiop.rpmachine.economy.jobs.restrictions.BreedingRestriction;
import net.zyuiop.rpmachine.economy.jobs.restrictions.EnchantingRestriction;
import net.zyuiop.rpmachine.economy.jobs.restrictions.FarmingRestriction;

/**
 * @author Louis Vialar
 */
public enum JobRestrictions {
    ENCHANTING(EnchantingRestriction.class),
    FARMING(FarmingRestriction.class),
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
