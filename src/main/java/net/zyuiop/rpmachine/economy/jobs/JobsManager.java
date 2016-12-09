package net.zyuiop.rpmachine.economy.jobs;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.util.*;

public class JobsManager {
	private final RPMachine rpMachine;
	private HashMap<String, Job> jobs = new HashMap<>();

	public JobsManager(RPMachine pl) {
		this.rpMachine = pl;
		for (Map<?, ?> map : rpMachine.getConfig().getMapList("jobs")) {
			String name = (String) map.get("name");
			String description = (String) map.get("description");
			HashSet<Material> materials = new HashSet<>();
			for (Object material : (List<?>) map.get("items")) {
				try {
					Material mat = null;
					if (material instanceof String) {
						mat = Material.valueOf((String) material);
					} else if (material instanceof  Integer) {
						mat = Material.getMaterial((Integer)material);
					}

					if (mat != null)
						materials.add(mat);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			jobs.put(name, new Job(name, description, materials));
		}
	}

	public Job getJob(UUID player) {
		PlayerData data = RPMachine.database().getPlayerData(player);
		String job = data.getJob();
		if (job == null)
			return null;
		Bukkit.getLogger().info("Job found : " + job);
		return getJob(job);
	}

	public HashMap<String, Job> getJobs() {
		return jobs;
	}

	public Job getJob(String job) {
		return jobs.get(job);
	}
}
