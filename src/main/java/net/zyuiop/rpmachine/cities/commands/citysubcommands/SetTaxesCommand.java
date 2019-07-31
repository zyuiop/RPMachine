package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.economy.EconomyManager;
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
        return "<taxes par block>";
    }

    @Override
    public String getDescription() {
        return "modifie les taxes de votre ville (en $/bloc de surface)";
    }

    @Override
    public boolean requiresCouncilPrivilege() {
        return true;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Arguments incorrects.");
            return false;
        } else {
            try {
                Double value = Double.valueOf(args[0]);
                if (value > citiesManager.getFloor(city).getMaxtaxes()) {
                    player.sendMessage(ChatColor.RED + "Votre montant est supérieur au montant maximal pour votre palier.");
                }
                city.setTaxes(value);
                citiesManager.saveCity(city);
                player.sendMessage(ChatColor.GREEN + "Les impôts sont désormais de " + value + " " + EconomyManager.getMoneyName() + "/bloc");
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Le montant est incorrect.");
            }
            return true;
        }
    }
}
