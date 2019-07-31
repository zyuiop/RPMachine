package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class SetTypeCommand implements CityMemberSubCommand {

    private final CitiesManager citiesManager;

    public SetTypeCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }


    @Override
    public String getUsage() {
        return "<private|public>";
    }

    @Override
    public String getDescription() {
        return "modifie le type de la ville";
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Utilisation incorrecte : /city settype " + getUsage());
            return false;
        } else {
            String newType = args[0];
            if (newType.equalsIgnoreCase("private")) {
                city.setRequireInvite(true);
                citiesManager.saveCity(city);
                player.sendMessage(ChatColor.GREEN + "Votre ville est désormais " + ChatColor.RED + "Privée" + ChatColor.GREEN + ". Les joueurs ne pourront la rejoindre qu'avec une invitation.");
            } else if (newType.equalsIgnoreCase("public")) {
                city.setRequireInvite(false);
                citiesManager.saveCity(city);
                player.sendMessage(ChatColor.GREEN + "Votre ville est désormais Publique. Les joueurs pourront la rejoindre librement.");
            } else {
                player.sendMessage(ChatColor.RED + "Type fourni invalide.");
            }
        }

        return true;
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.SET_PRIVACY;
    }
}
