package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.Line;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.City;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BordersCommand implements CityMemberSubCommand {
    private final Map<UUID, BukkitTask> tasks = new HashMap<>();

    @Override
    public String getUsage() {
        return "[toggle]";
    }

    @Override
    public String getDescription() {
        return "affiche les bordures de la ville";
    }

    @Override
    public boolean requiresCouncilPrivilege() {
        return true;
    }

    @Override
    public boolean run(Player player, City city, String command, String subcommand, String[] args) {
        List<Line> borders = city.getBorders();

        if (args.length > 0 && args[0].equalsIgnoreCase("toggle")) {
            if (tasks.containsKey(player.getUniqueId())) {
                tasks.remove(player.getUniqueId()).cancel();
                player.sendMessage(ChatColor.GREEN + "Bordures désactivées.");
            } else {
                tasks.put(player.getUniqueId(), Bukkit.getScheduler().runTaskTimer(RPMachine.getInstance(), () -> borders.forEach(b -> b.display(player)), 0L, 80L));
                player.sendMessage(ChatColor.GREEN + "Bordures affichées. Relancez cette commande pour désactiver.");
            }
        } else {
            borders.forEach(b -> b.display(player));
            player.sendMessage(ChatColor.GREEN + "Bordures affichées. Utilisez /" + command + " " + subcommand + " toggle pour maintenir affiché.");
        }

        return true;
    }
}
