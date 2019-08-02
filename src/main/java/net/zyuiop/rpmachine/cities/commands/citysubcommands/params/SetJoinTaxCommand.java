package net.zyuiop.rpmachine.cities.commands.citysubcommands.params;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class SetJoinTaxCommand implements CityMemberSubCommand {

    private final CitiesManager citiesManager;

    public SetJoinTaxCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "[taxe de citoyenneté]";
    }

    @Override
    public String getDescription() {
        return "modifie le prix payé pour devenir citoyen";
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.SET_TAXES;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Taxe de citoyenneté actuelle : " + ChatColor.GOLD + city.getJoinTax() + " " + RPMachine.getCurrencyName());
            return true;
        } else {
            try {
                int value = Integer.parseInt(args[0]);
                if (value > citiesManager.getFloor(city).getMaxJoinTax()) {
                    player.sendMessage(ChatColor.RED + "Votre montant est supérieur au montant maximal pour votre palier.");
                    return true;
                }
                city.setJoinTax(value);
                citiesManager.saveCity(city);
                player.sendMessage(ChatColor.GREEN + "La taxe de citoyenneté est désormais de " + ChatColor.DARK_GREEN + value + " " + RPMachine.getCurrencyName());
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Le montant est incorrect.");
            }
            return true;
        }
    }
}
