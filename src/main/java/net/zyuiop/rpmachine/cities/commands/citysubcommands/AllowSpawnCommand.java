package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AllowSpawnCommand implements SubCommand {

    private final CitiesManager citiesManager;

    public AllowSpawnCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<ville> [yes/no]";
    }

    @Override
    public String getDescription() {
        return "autorise une ville à être le spawn du serveur";
    }

    @Override
    public boolean canUse(Player player) {
        return player.hasPermission("admin.setallowspawn");
    }

    private String toDisplayable(boolean status) {
        return status ? (ChatColor.GREEN + "peut être un spawn") : (ChatColor.RED + "ne peut pas être un spawn");
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Liste des villes autorisées à être spawn :");
            citiesManager.getSpawnCities().forEach(c -> player.sendMessage(ChatColor.YELLOW + " - " + c.getChatColor() + c.getCityName()));
            return true;
        }

        City target = citiesManager.getCity(args[0]);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Aucune ville de ce nom là n'existe.");
            return false;
        }

        boolean isAllow = target.isAllowSpawn();

        if (args.length > 1) {
            String allow = args[1].toLowerCase();

            if (allow.equals("yes") || allow.equalsIgnoreCase("true")) {
                if (target.getSpawn() == null) {
                    player.sendMessage(ChatColor.RED + "La ville sélectionnée n'a pas de spawn.");
                    return false;
                }
                target.setAllowSpawn(true);
                target.save();
            } else if (allow.equals("no") || allow.equals("false")) {
                target.setAllowSpawn(false);
                target.save();
            } else {
                player.sendMessage(ChatColor.RED + "Paramètre incorrect " + allow + ". Valeurs aceptées: yes, no");
                return false;
            }

            player.sendMessage(ChatColor.YELLOW + "Statut modifié. La ville " + toDisplayable(target.isAllowSpawn()) + ChatColor.YELLOW + ". Statut précédent : " + toDisplayable(isAllow));
        } else {
            player.sendMessage(ChatColor.YELLOW + "Statut actuel de la ville : " + toDisplayable(isAllow));
            player.sendMessage(ChatColor.GRAY + "Pour modifier, utilisez /" + command + " " + subCommand + " " + args[0] + " " + (isAllow ? "no" : "yes"));
        }

        return true;
    }
}
