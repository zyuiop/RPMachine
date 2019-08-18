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

public class JobsManager {
	private final RPMachine rpMachine;
	private HashMap<String, Job> jobs = new HashMap<>();
	private Set<JobRestrictions> enabledRestrictions = new HashSet<>();
	private Set<Material> restrictSale = new HashSet<>();
	private Set<Material> restrictCraft = new HashSet<>();
	private Set<Material> restrictUse = new HashSet<>();

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
			Job.Builder builder = Job.Builder.aJob()
					.withJobName(name)
					.withJobDescription(description);

			Set<Material> restrictSale = parseMaterialSet((List<?>) map.get("restrictSale"));
			Set<Material> restrictCraft = parseMaterialSet((List<?>) map.get("restrictCraft"));
			Set<Material> restrictUse = parseMaterialSet((List<?>) map.get("restrictUse"));

			builder = builder
					.withRestrictSale(restrictSale)
					.withRestrictCraft(restrictCraft)
					.withRestrictUse(restrictUse);


			HashSet<JobRestrictions> restrictions = new HashSet<>();
			if (map.containsKey("restrictions")) {
				for (Object rest : (List<?>) map.get("restrictions"))
					restrictions.add(JobRestrictions.valueOf((String) rest));
			}

			builder = builder.withRestrictions(restrictions);

			enabledRestrictions.addAll(restrictions);
			this.restrictCraft.addAll(restrictCraft);
			this.restrictUse.addAll(restrictUse);
			this.restrictSale.addAll(restrictSale);

			jobs.put(name.toLowerCase(), builder.build());
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

	public boolean isFreeToSell(Material m) {
		return !restrictSale.contains(m) && isFreeToCraft(m);
	}

	public boolean isFreeToCraft(Material m) {
		return !restrictCraft.contains(m) && isFreeToUse(m);
	}

	public boolean isFreeToUse(Material m) {
		return !restrictUse.contains(m);
	}

	public boolean canSell(Player p, Material m) {
		return isFreeToSell(m) || (getJob(p) != null && getJob(p).canSell(m));
	}

	public boolean canCraft(Player p, Material m) {
		return isFreeToCraft(m) || (getJob(p) != null && getJob(p).canCraft(m));
	}

	public boolean canUse(Player p, Material m) {
		return isFreeToUse(m) || (getJob(p) != null && getJob(p).canUse(m));
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

	public void printAvailableJobsToSell(Material item, Player player) {
		printAvailableJobs(j -> j.canSell(item), "Cet objet ne peut vendu que par certains métiers.", player);
	}

	public void printAvailableJobsToCraft(Material item, Player player) {
		printAvailableJobs(j -> j.canCraft(item), "Cet objet ne peut être crafté et vendu que par certains métiers.", player);
	}

	public void printAvailableJobsToUse(Material block, Player player) {
		printAvailableJobs(j -> j.canUse(block), "Ce block ne peut être crafté, vendu, placé et utilisé que par certains métiers.", player);
	}

	public HashMap<String, Job> getJobs() {
		return jobs;
	}

	public Job getJob(String job) {
		return jobs.get(job.toLowerCase());
	}

	public int getQuitPrice() {
		return quitPrice;
	}

	public int getQuitFrequency() {
		return quitFrequency;
	}
}
