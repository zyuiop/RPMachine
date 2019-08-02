package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SetMayorCommand implements CityMemberSubCommand {

    private final CitiesManager citiesManager;

    public SetMayorCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }


    @Override
    public String getUsage() {
        return "<pseudo>";
    }

    @Override
    public String getDescription() {
        return "nomme un nouveau maire";
    }

    @Override
    public boolean requiresMayorPrivilege() {
        return true;
    }

    @Override
    public boolean run(Player player, City city, String command, String subcommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Utilisation incorrecte : /city setmayor " + getUsage());
        } else {
            String newMayor = args[0];
            UUID id = RPMachine.database().getUUIDTranslator().getUUID(newMayor);
            if (id == null) {
                player.sendMessage(ChatColor.RED + "Ce joueur n'a pas été trouvé.");
            } else {
                if (!city.getInhabitants().contains(id)) {
                    player.sendMessage(ChatColor.RED + "Ce joueur n'est pas citoyen de votre ville.");
                } else {
                    city.setMayor(id);
                    city.getCouncils().add(id);
                    player.sendMessage(ChatColor.GREEN + "Le maire a été modifié.");
                    citiesManager.saveCity(city);
                }
            }
        }

        return true;
    }
}
