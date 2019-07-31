package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.UUID;

public class CouncilCommand implements CityMemberSubCommand {

    private final CitiesManager citiesManager;

    public CouncilCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public boolean requiresMayorPrivilege() {
        return true;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String[] args) {
        if (args.length == 1 && args[0].equals("list")) {
            player.sendMessage(ChatColor.GOLD + "-----[ Liste des Conseillers ]-----");
            for (UUID council : city.getCouncils()) {
                String name = RPMachine.database().getUUIDTranslator().getName(council, false);
                if (name != null)
                    player.sendMessage(ChatColor.YELLOW + " - " + name);
            }
            return true;
        } else if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Arguments incorrects.");
            return false;
        } else {
            String type = args[0];
            String pseudo = args[1];
            if (!type.equalsIgnoreCase("add") && !type.equalsIgnoreCase("remove")) {
                player.sendMessage(ChatColor.RED + "Arguments incorrects.");
                return false;
            }

            UUID id = RPMachine.database().getUUIDTranslator().getUUID(pseudo, true);
            if (id == null) {
                player.sendMessage(ChatColor.RED + "Ce joueur n'a pas été trouvé.");
            } else if (id == city.getMayor()) {
                player.sendMessage(ChatColor.RED + "Impossible d'affecter le maire de la ville !");
            } else {
                if (type.equalsIgnoreCase("add")) {
                    city.addCouncil(id);
                    player.sendMessage(ChatColor.GREEN + "Ce joueur est désormais conseiller !");
                    citiesManager.saveCity(city);
                } else {
                    city.removeCouncil(id);
                    player.sendMessage(ChatColor.GREEN + "Ce joueur n'est désormais plus conseiller !");
                    citiesManager.saveCity(city);
                }
            }
            return true;
        }
    }

    @Override
    public String getUsage() {
        return "<add|remove|list> <pseudo>";
    }

    @Override
    public String getDescription() {
        return "ajoute, liste, ou supprime un adjoint dans votre ville";
    }
}
