package net.zyuiop.rpmachine.jobs;

import com.google.common.collect.Sets;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class JobsManager {
    private HashMap<String, Job> jobs = new HashMap<>();
    private Set<JobRestrictions> enabledRestrictions = new HashSet<>();
    private Set<Material> restrictCraft = new HashSet<>();
    private Set<Material> restrictUse = new HashSet<>();
    private Map<Material, Integer> restrictCollect = new HashMap<>();

    private int quitPrice;
    private int quitFrequency;

    public JobsManager(RPMachine pl) {
        Bukkit.getPluginManager().registerEvents(new JobsListener(this), pl);

        for (Map<?, ?> map : pl.getConfig().getMapList("jobs")) {
            String name = (String) map.get("name");
            String description = (String) map.get("description");
            Job.Builder builder = Job.Builder.aJob()
                    .withJobName(name)
                    .withJobDescription(description);

            Set<Material> restrictCraft = parseMaterialSet((List<?>) map.get("restrictCraft"));
            Set<Material> restrictUse = parseMaterialSet((List<?>) map.get("restrictUse"));
            Map<String, Integer> restrictCollectBase = (Map<String, Integer>) map.get("restrictCollect");

            if (restrictCollectBase == null) restrictCollectBase = Collections.emptyMap();

            Map<Material, Integer> restrictCollect = restrictCollectBase.entrySet().stream()
                    .filter(entry -> Material.getMaterial(entry.getKey()) != null)
                    .collect(Collectors.toMap(
                            entry -> Material.getMaterial(entry.getKey()),
                            Map.Entry::getValue
                    ));


            builder
                    .withRestrictCraft(restrictCraft)
                    .withRestrictUse(restrictUse)
                    .withRestrictCollect(restrictCollect);


            HashSet<JobRestrictions> restrictions = new HashSet<>();
            if (map.containsKey("restrictions")) {
                for (Object rest : (List<?>) map.get("restrictions"))
                    restrictions.add(JobRestrictions.valueOf((String) rest));
            }

            builder.withRestrictions(restrictions);

            enabledRestrictions.addAll(restrictions);
            this.restrictCraft.addAll(restrictCraft);
            this.restrictUse.addAll(restrictUse);
            this.restrictCollect.putAll(restrictCollect);

            this.jobs.put(name.toLowerCase(), builder.build());
        }

        // Enable enabled restrictions
        enabledRestrictions.forEach(r -> {
            Bukkit.getLogger().info("Enabling job restriction " + r);
            JobRestriction rest = r.newInstance();
            Bukkit.getPluginManager().registerEvents(rest, pl);
        });

        quitPrice = pl.getConfig().getInt("jobsquit.price", 100);
        quitFrequency = pl.getConfig().getInt("jobsquit.mindays", 7);
    }

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

    public boolean isFreeToCraft(Material m) {
        return !restrictCraft.contains(m) && isFreeToUse(m);
    }

    public boolean isFreeToUse(Material m) {
        return !restrictUse.contains(m);
    }

    public int getCollectLimit(Material m) {
        return restrictCollect.getOrDefault(m, -1);
    }

    public boolean canCraft(Player p, Material m) {
        return isFreeToCraft(m) || (getJob(p) != null && getJob(p).canCraft(m));
    }

    public boolean canCollect(Player p, Material m) {
        return getCollectLimit(m) < 0 || (getJob(p) != null && getJob(p).canCollect(m));
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

    public void printAvailableJobsToCraft(Material item, Player player) {
        printAvailableJobs(j -> j.canCraft(item), "Cet objet ne peut être crafté que par certains métiers.", player);
    }

    public void printAvailableJobsToUse(Material block, Player player) {
        printAvailableJobs(j -> j.canUse(block), "Ce block ne peut être crafté, placé et utilisé que par certains métiers.", player);
    }

    public void printAvailableJobsToCollect(Material m, Player p, int limit) {
        String availableJobs =
                StringUtils.join(jobs.values().stream().filter(j -> j.canCollect(m)).map(Job::getJobName).collect(Collectors.toList()),
                        ChatColor.GOLD + ", " + ChatColor.YELLOW);

        p.sendActionBar(ChatColor.RED + "Limite quotidienne de collecte dépassée pour " + m + ". Devenez " + availableJobs);
        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
    }

    public void printAlmostCollectedAll(Material m, Player p, int limit, int current) {
        printAvailableJobs(j -> j.canCollect(m), ChatColor.GOLD + "Attention, vous avez collecté " + current + " " + m + " sur un maximum de " + limit + "/jour.", p);
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

    public void checkPlayerJob(Player player, PlayerData data) {
        if (data.getJob() != null && getJob(data.getJob()) == null) {
            player.sendMessage(ChatColor.RED + "Votre métier a été supprimé depuis votre dernière connexion !");
            player.sendMessage(ChatColor.GRAY + "Vous pouvez changer gratuitement de métier via " + ChatColor.YELLOW + "/jobs choose");
            data.setJob(null);
        }
    }

    public Map<Job, Long> getJobsQuantities() {
        long last48 = System.currentTimeMillis() - 48L * 3600L * 1000L;

        Map<Job, Long> jobs = RPMachine.getInstance().getDatabaseManager()
                .getPlayers(p -> p.getLastLogin() >= last48)
                .stream()
                .map(PlayerData::getJob).filter(Objects::nonNull)
                .map(this::getJob).filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        for (Job j : this.jobs.values())
            if (!jobs.containsKey(j))
                jobs.put(j, 0L);

        return jobs;
    }

    /**
     * Get the proportion of jobs among players that logged in in the last 48 hours
     */
    public Map<Job, Double> getJobsProportion() {
        Map<Job, Long> map = getJobsQuantities();
        long totalNum = map.values().stream().mapToLong(l -> l).sum();

        return map.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, e -> {
                    if (totalNum < 15) return 0D; // if less than 15 players, stats are not significant
                    else return e.getValue().doubleValue() / totalNum;
                }
        ));
    }

    public double getMaxJobProportion() {
        int nbJobs = jobs.size();
        double baseProportion = 1.0 / nbJobs;

        return (1.0 - baseProportion / 2) / (nbJobs - 1); // Less used job must never have less than half of base average
    }

    public Map<Job, Boolean> getAvailableJobs() {
        double p = getMaxJobProportion();

        return getJobsProportion().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, e -> e.getValue() < p
        ));
    }
}
