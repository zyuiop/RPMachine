package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.Plot;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class RemoveCommand implements CityMemberSubCommand {
    private final CitiesManager citiesManager;

    public RemoveCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<nom>";
    }

    @Override
    public String getDescription() {
        return "supprime la parcelle <nom>";
    }

    @Override
    public boolean requiresCouncilPrivilege() {
        return true;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Argument manquant.");
            return false;
        } else {
            Plot plot = city.getPlots().get(args[0]);
            if (plot == null)
                player.sendMessage(ChatColor.RED + "Cette parcelle n'existe pas.");
            else {
                if (plot.getOwner() != null && (args.length < 2 || !args[1].equalsIgnoreCase("override"))) {
                    player.sendMessage(ChatColor.RED + "Cette parcelle est habitée !");
                    player.sendMessage(ChatColor.RED + "Pour supprimer quand même, ajoutez 'override' aux arguments de la commande.");
                }
                city.getPlots().remove(plot.getPlotName());
                citiesManager.saveCity(city);
                player.sendMessage(ChatColor.GREEN + "La parcelle a bien été supprimée.");
            }

            return true;
        }
    }
}
