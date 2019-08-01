package net.zyuiop.rpmachine.economy.jobs;

import org.bukkit.Material;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Job {
	protected String jobName;
	protected String jobDescription;
	protected Set<Material> restrictedItems = new HashSet<>();
	protected Set<Material> restrictedBlocks = new HashSet<>();
	protected Set<JobRestrictions> restrictions = new HashSet<>();

	public Job(String jobName, String jobDescription, Set<Material> restrictedItems, Set<Material> restrictedBlocks, Set<JobRestrictions> restrictions) {
		this.jobName = jobName;
		this.jobDescription = jobDescription;
		this.restrictedItems.addAll(restrictedItems);
		this.restrictedBlocks.addAll(restrictedBlocks);
		this.restrictions.addAll(restrictions);
	}

	public Set<JobRestrictions> getRestrictions() {
		return Collections.unmodifiableSet(restrictions);
	}

	public Set<Material> getRestrictedItems() {
		return restrictedItems;
	}

	public Set<Material> getRestrictedBlocks() {
		return restrictedBlocks;
	}

	public String getJobName() {
		return jobName;
	}

	public String getJobDescription() {
		return jobDescription;
	}
}
