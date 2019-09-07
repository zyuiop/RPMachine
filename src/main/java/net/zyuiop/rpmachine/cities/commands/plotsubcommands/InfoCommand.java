package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import joptsimple.internal.Strings;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.entities.LegalEntity;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class InfoCommand implements SubCommand {
    private final CitiesManager citiesManager;

    public InfoCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "affiche des informations sur la parcelle où vous vous trouvez";
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {

        City city = citiesManager.getCityHere(player.getLocation().getChunk());
        if (city == null) {
            player.sendMessage(ChatColor.RED + "Vous ne vous trouvez pas dans une vile.");
        } else {
            Optional<Plot> oPlot = city.getPlotAt(player.getLocation());
            if (!oPlot.isPresent()) {
                player.sendMessage(ChatColor.RED + "Vous ne vous trouvez pas dans une parcelle.");
            } else {
                Plot plot = oPlot.get();
                player.sendMessage(ChatColor.GOLD + "-----[ Informations Parcelle ]-----");
                player.sendMessage(ChatColor.YELLOW + "Nom : " + plot.getPlotName());
                player.sendMessage(ChatColor.YELLOW + "Ville : " + city.getCityName());
                player.sendMessage(ChatColor.YELLOW + "Surface : " + plot.getArea().computeArea() + " blocs");
                player.sendMessage(ChatColor.YELLOW + "Impots : " + plot.getArea().computeArea() * city.getTaxes() + " " + RPMachine.getCurrencyName());

                if (plot.isDueForDeletion())
                    player.sendMessage(ChatColor.YELLOW + "Suppression prévue le : " + ChatColor.RED + plot.getDeletionDateString());

                LegalEntity proprio = plot.owner();
                if (proprio == null) {
                    player.sendMessage(ChatColor.YELLOW + "Propriétaire : " + ChatColor.RED + "Aucun");
                } else {
                    String name = proprio.displayable();
                    if (name == null) {
                        player.sendMessage(ChatColor.YELLOW + "Propriétaire : " + ChatColor.GOLD + "Inconnu");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "Propriétaire : " + ChatColor.GREEN + name);
                    }
                }

                ArrayList<String> members = new ArrayList<>();
                for (UUID id : plot.getPlotMembers()) {
                    String name = RPMachine.database().getUUIDTranslator().getName(id);
                    if (name != null)
                        members.add(name);
                }

                if (members.size() > 0) {
                    String mem = Strings.join(members, ", ");
                    player.sendMessage(ChatColor.YELLOW + "Membres : " + mem);
                }
            }
        }

        return true;
    }
}
