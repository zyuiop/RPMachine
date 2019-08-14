package net.zyuiop.rpmachine.cities.commands.citysubcommands.params;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class SetTaxesCommand implements CityMemberSubCommand {

    private final CitiesManager citiesManager;

    public SetTaxesCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "[taxes par block]";
    }

    @Override
    public String getDescription() {
        return "modifie les taxes de votre ville (en $/bloc de surface)";
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.SET_TAXES;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Taxes actuelles : " + ChatColor.GOLD + city.getTaxes() + " " + RPMachine.getCurrencyName() + "/bloc");
            return true;
        } else {
            try {
                Double value = Double.valueOf(args[0]);
                if (value > citiesManager.getFloor(city).getMaxtaxes()) {
                    player.sendMessage(ChatColor.RED + "Votre montant est supérieur au montant maximal pour votre palier.");
                    return true;
                }
                city.setTaxes(value);
                citiesManager.saveCity(city);
                player.sendMessage(ChatColor.GREEN + "Les impôts sont désormais de " + value + " " + RPMachine.getCurrencyName() + "/bloc");
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Le montant est incorrect.");
            }
            return true;
        }
    }
}
