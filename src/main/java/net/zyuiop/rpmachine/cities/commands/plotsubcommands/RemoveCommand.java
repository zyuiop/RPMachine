package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.utils.Messages;
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
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Argument manquant.");
            return false;
        } else {
            Plot plot = city.getPlot(args[0]);
            if (plot == null)
                player.sendMessage(ChatColor.RED + "Cette parcelle n'existe pas.");
            else {
                if (plot.getOwner() != null && (args.length < 2 || !args[1].equalsIgnoreCase("override"))) {
                    player.sendMessage(ChatColor.RED + "Cette parcelle est habitée !");

                    if (city.hasPermission(player, CityPermissions.DELETE_OCCUPIED_PLOT)) {
                        player.sendMessage(ChatColor.RED + "Pour supprimer quand même, ajoutez 'override' aux arguments de la commande.");

                        if (! city.getPoliticalSystem().allowInstantPlotDelete()) {
                            player.sendMessage(ChatColor.RED + "La suppression sera effective d'ici une semaine, sauf si le joueur quitte sa parcelle avant.");
                        }
                    }
                    return true;
                } else if (plot.getOwner() != null && !city.hasPermission(player, CityPermissions.DELETE_OCCUPIED_PLOT)) {
                    player.sendMessage(ChatColor.RED + "Vous ne pouvez pas supprimer une parcelle occupée.");
                    return true;
                } else if (plot.getOwner() == null && !city.hasPermission(player, CityPermissions.DELETE_EMPTY_PLOT)) {
                    player.sendMessage(ChatColor.RED + "Vous ne pouvez pas supprimer une parcelle vide.");
                    return true;
                }

                if (plot.getOwner() != null && ! city.getPoliticalSystem().allowInstantPlotDelete()) {
                    plot.setForDeletion();
                    plot.sendDeletionWarning(city.getCityName());
                    player.sendMessage(ChatColor.GREEN + "La parcelle sera supprimée le " + plot.getDeletionDate());
                } else {
                    city.removePlot(plot.getPlotName());
                    player.sendMessage(ChatColor.GREEN + "La parcelle a bien été supprimée.");
                }
                citiesManager.saveCity(city);
            }

            return true;
        }
    }
}
