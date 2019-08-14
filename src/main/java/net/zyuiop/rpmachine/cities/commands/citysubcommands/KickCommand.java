package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class KickCommand implements CityMemberSubCommand {

    private final CitiesManager citiesManager;

    public KickCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<pseudo>";
    }

    @Override
    public String getDescription() {
        return "expulse un citoyen de la ville";
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.KICK_MEMBER;
    }

    @Override
    public boolean run(Player player, City city, String command, String subcommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Le pseudo du joueur est manquant.");
            return false;
        } else {
            String pseudo = args[0];
            UUID id = RPMachine.database().getUUIDTranslator().getUUID(pseudo);
            if (id == null) {
                player.sendMessage(ChatColor.RED + "Ce joueur n'a pas été trouvé.");
            } else {
                if (!city.getInhabitants().contains(id)) {
                    player.sendMessage(ChatColor.RED + "Ce joueur n'est pas membre de votre ville.");
                } else {
                    city.getInhabitants().remove(id);
                    citiesManager.saveCity(city);
                    player.sendMessage(ChatColor.GREEN + "Le joueur a bien été exclus de votre ville.");
                }
            }
            return true;
        }
    }
}
