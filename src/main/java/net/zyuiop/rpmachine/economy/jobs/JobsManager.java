package net.zyuiop.rpmachine.economy.jobs;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

// TODO: on init, find restrictions, and if they are used init their listener
public class JobsManager {
	private final RPMachine rpMachine;
	private HashMap<String, Job> jobs = new HashMap<>();
	private Set<JobRestrictions> enabledRestrictions = new HashSet<>();

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
						if (((String) material).contains(":")) {
							mat = Material.matchMaterial((String) material);
						} else
							mat = Material.getMaterial((String) material);
					}

					if (mat != null)
						materials.add(mat);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			HashSet<JobRestrictions> restrictions = new HashSet<>();
			if (map.containsKey("restrictions")) {
				for (Object rest : (List<?>) map.get("restrictions"))
					restrictions.add(JobRestrictions.valueOf((String) rest));
			}

			enabledRestrictions.addAll(restrictions);

			jobs.put(name, new Job(name, description, materials, restrictions));
		}

		// Enable enabled restrictions
		enabledRestrictions.forEach(r -> {
			Bukkit.getLogger().info("Enabling job restriction " + r);
			JobRestriction rest = r.newInstance();
			Bukkit.getPluginManager().registerEvents(rest, pl);
		});
	}

	public Job getJob(Player player) {
		return getJob(player.getUniqueId());
	}

	public boolean isRestrictionEnabled(JobRestrictions r) {
		return enabledRestrictions.contains(r);
	}

	public boolean isRestrictionAllowed(Player p, JobRestrictions r) {
		if (!isRestrictionEnabled(r))
			return false;

		return getJob(p) != null && getJob(p).getRestrictions().contains(r);
	}

	public Job getJob(UUID player) {
		PlayerData data = RPMachine.database().getPlayerData(player);
		String job = data.getJob();
		if (job == null)
			return null;
		return getJob(job);
	}

	public void printAvailableJobs(JobRestrictions restrictedAction, Player player) {
		String availableJobs =
				StringUtils.join(getJobs(restrictedAction).stream().map(Job::getJobName).collect(Collectors.toList()),
						ChatColor.GOLD + ", " + ChatColor.YELLOW);

		player.sendMessage(ChatColor.RED + "Cette action est restreinte et nécessite d'avoir le travail adéquat. Métiers autorisés : " + ChatColor.YELLOW + availableJobs);

	}

	public List<Job> getJobs(JobRestrictions restrictions) {
		return jobs.values().stream().filter(j -> j.getRestrictions().contains(restrictions)).collect(Collectors.toList());
	}

	public HashMap<String, Job> getJobs() {
		return jobs;
	}

	public Job getJob(String job) {
		return jobs.get(job);
	}
}
