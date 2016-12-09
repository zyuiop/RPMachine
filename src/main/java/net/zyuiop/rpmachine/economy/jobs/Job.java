package net.zyuiop.rpmachine.economy.jobs;

import org.bukkit.Material;

import java.util.HashSet;

public class Job {
	protected String jobName;
	protected String jobDescription;
	protected HashSet<Material> materials;

	public Job(String jobName, String jobDescription, HashSet<Material> materials) {
		this.jobName = jobName;
		this.jobDescription = jobDescription;
		this.materials = materials;
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

	public HashSet<Material> getMaterials() {
		return materials;
	}

	public void setMaterials(HashSet<Material> materials) {
		this.materials = materials;
	}
}
