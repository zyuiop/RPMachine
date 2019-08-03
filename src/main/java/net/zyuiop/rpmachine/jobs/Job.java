package net.zyuiop.rpmachine.jobs;

import org.bukkit.Material;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Job {
    protected String jobName;
    protected String jobDescription;

    // TODO: replace with 3 levels of restriction: restrictSale, restrictCraft, restrictUse (restricts use and place, for blocks only). Each inheriting the previous level(s)
    protected Set<Material> restrictSale = new HashSet<>();
    protected Set<Material> restrictCraft = new HashSet<>();
    protected Set<Material> restrictUse = new HashSet<>();
    protected Set<JobRestrictions> restrictions = new HashSet<>();

    public Job(String jobName, String jobDescription, Set<Material> restrictSale, Set<Material> restrictCraft, Set<Material> restrictUse, Set<JobRestrictions> restrictions) {
        this.jobName = jobName;
        this.jobDescription = jobDescription;
        this.restrictSale.addAll(restrictSale);
        this.restrictCraft.addAll(restrictCraft);
        this.restrictUse.addAll(restrictUse);
        this.restrictions.addAll(restrictions);
    }

    public Set<JobRestrictions> getRestrictions() {
        return Collections.unmodifiableSet(restrictions);
    }

    public Set<Material> getRestrictSale() {
        return restrictSale;
    }

    public Set<Material> getRestrictCraft() {
        return restrictCraft;
    }

    public Set<Material> getRestrictUse() {
        return restrictUse;
    }

    public boolean canSell(Material material) {
        return restrictSale.contains(material) || canCraft(material);
    }

    public boolean canCraft(Material material) {
        return restrictCraft.contains(material) || canUse(material);
    }

    public boolean canUse(Material material) {
        return restrictUse.contains(material);
    }

    public String getJobName() {
        return jobName;
    }

    public String getJobDescription() {
        return jobDescription;
    }
}
