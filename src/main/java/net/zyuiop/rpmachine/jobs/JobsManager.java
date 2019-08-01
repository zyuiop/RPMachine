package net.zyuiop.rpmachine.jobs;

import com.google.common.collect.Sets;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// TODO: on init, find restrictions, and if they are used init their listener
public class JobsManager {
	private final RPMachine rpMachine;
	private HashMap<String, Job> jobs = new HashMap<>();
	private Set<JobRestrictions> enabledRestrictions = new HashSet<>();
	private Set<Material> restrictedItems = new HashSet<>(); // items that can only be crafted/sold by specific job
	private Set<Material> restrictedBlocks = new HashSet<>(); // blocks that can only be placed/used by specific job

	private int quitPrice;
	private int quitFrequency;

	private Set<Material> parseMaterialSet(List<?> list) {
		if (list == null) return Sets.newHashSet();

		HashSet<Material> materials = new HashSet<>();
		for (Object material : list) {
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

		return materials;
	}

	public JobsManager(RPMachine pl) {
		this.rpMachine = pl;

		Bukkit.getPluginManager().registerEvents(new JobsListener(this), pl);

		for (Map<?, ?> map : rpMachine.getConfig().getMapList("jobs")) {
			String name = (String) map.get("name");
			String description = (String) map.get("description");
			Set<Material> restrictedItems = parseMaterialSet((List<?>) map.get("restrictedItems"));
			Set<Material> restrictedBlocks = parseMaterialSet((List<?>) map.get("restrictedBlocks"));


			HashSet<JobRestrictions> restrictions = new HashSet<>();
			if (map.containsKey("restrictions")) {
				for (Object rest : (List<?>) map.get("restrictions"))
					restrictions.add(JobRestrictions.valueOf((String) rest));
			}

			enabledRestrictions.addAll(restrictions);
			this.restrictedBlocks.addAll(restrictedBlocks);
			this.restrictedItems.addAll(restrictedItems);

			jobs.put(name, new Job(name, description, restrictedItems, restrictedBlocks, restrictions));
		}

		// Enable enabled restrictions
		enabledRestrictions.forEach(r -> {
			Bukkit.getLogger().info("Enabling job restriction " + r);
			JobRestriction rest = r.newInstance();
			Bukkit.getPluginManager().registerEvents(rest, pl);
		});

		quitPrice = rpMachine.getConfig().getInt("jobsquit.price", 100);
		quitFrequency = rpMachine.getConfig().getInt("jobsquit.mindays", 7);
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

	public boolean isItemRestricted(Material m) {
		return restrictedItems.contains(m);
	}

	public boolean isItemAllowed(Player p, Material m) {
		return !isItemRestricted(m) || (getJob(p) != null && getJob(p).getRestrictedItems().contains(m));
	}

	public boolean isBlockRestricted(Material m) {
		return restrictedBlocks.contains(m);
	}

	public boolean isBlockAllowed(Player p, Material m) {
		return !isBlockRestricted(m) || (getJob(p) != null && getJob(p).getRestrictedBlocks().contains(m));
	}

	public Job getJob(UUID player) {
		PlayerData data = RPMachine.database().getPlayerData(player);
		String job = data.getJob();
		if (job == null)
			return null;
		return getJob(job);
	}

	public void printAvailableJobs(JobRestrictions restrictedAction, Player player) {
		printAvailableJobs(j -> j.getRestrictions().contains(restrictedAction), "Cette action est restreinte et nécessite d'avoir le travail adéquat.", player);
	}

	private void printAvailableJobs(Predicate<Job> filter, String message, Player player) {
		String availableJobs =
				StringUtils.join(jobs.values().stream().filter(filter).map(Job::getJobName).collect(Collectors.toList()),
						ChatColor.GOLD + ", " + ChatColor.YELLOW);

		player.sendMessage(ChatColor.RED + message + " Métiers autorisés : " + ChatColor.YELLOW + availableJobs);
	}

	public void printAvailableJobsForItem(Material item, Player player) {
		printAvailableJobs(j -> j.getRestrictedItems().contains(item), "Cet objet ne peut être crafté et vendu que par certains métiers.", player);
	}

	public void printAvailableJobsForBlock(Material block, Player player) {
		printAvailableJobs(j -> j.getRestrictedBlocks().contains(block), "Ce block ne peut être crafté, vendu, placé et utilisé que par certains métiers.", player);
	}

	public HashMap<String, Job> getJobs() {
		return jobs;
	}

	public Job getJob(String job) {
		return jobs.get(job);
	}

	public int getQuitPrice() {
		return quitPrice;
	}

	public int getQuitFrequency() {
		return quitFrequency;
	}
}
