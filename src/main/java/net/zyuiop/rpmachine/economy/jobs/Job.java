package net.zyuiop.rpmachine.economy.jobs;

import org.bukkit.Material;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Job {
	protected String jobName;
	protected String jobDescription;
	protected Set<Material> materials = new HashSet<>();
	protected Set<JobRestrictions> restrictions = new HashSet<>();

	public Job(String jobName, String jobDescription, HashSet<Material> materials, Set<JobRestrictions> restrictions) {
		this.jobName = jobName;
		this.jobDescription = jobDescription;
		this.materials.addAll(materials);
		this.restrictions.addAll(restrictions);
	}

	public Set<JobRestrictions> getRestrictions() {
		return Collections.unmodifiableSet(restrictions);
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
	}

	public Set<Material> getMaterials() {
		return materials;
	}

	public void setMaterials(HashSet<Material> materials) {
		this.materials = materials;
	}
}
