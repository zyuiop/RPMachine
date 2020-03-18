package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.cities.CityPlot;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.common.regions.Region;
import net.zyuiop.rpmachine.common.selections.PlayerSelection;
import net.zyuiop.rpmachine.common.selections.Selection;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.stream.StreamSupport;

public class CreateCommand implements CityMemberSubCommand {
    private final CitiesManager citiesManager;

    public CreateCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<nom> [groundtosky]";
    }

    @Override
    public String getDescription() {
        return "crée une parcelle sur la sélection, ou jusqu'au ciel si [groundtosky] est spécifiée";
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.CREATE_PLOT;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        if (PlayerSelection.getPlayerSelection(player) == null) {
            player.sendMessage(ChatColor.RED + "Vous n'avez sélectionné aucune région.");
            return false;
        } else {
            Selection selection = PlayerSelection.getPlayerSelection(player);

            try {
                Region area = selection.getRegion();
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "Syntaxe invalide : /plot create " + getUsage());
                } else {
                    String name = args[0];
                    if (!name.matches("^[a-zA-Z0-9]{3,16}$")) {
                        player.sendMessage(ChatColor.RED + "Le nom de parcelles doit être composé de 3 à 16 caractères alphanumériques.");
                        return false;
                    }

                    if (city.getPlot(name) != null) {
                        player.sendMessage(ChatColor.RED + "Ce nom de parcelle est déjà utilisé.");
                        return true;
                    }

                    if (args.length > 1 && args[1].equalsIgnoreCase("groundtosky")) {
                        area.expandY(-255);
                        area.expandY(255);

                        player.sendMessage(ChatColor.RED + "L'option groundtosky n'est plus supportée, utilisez plutôt " + ChatColor.YELLOW + "/sel expand");
                    }

                    boolean mustStop = StreamSupport.stream(area.spliterator(), false)
                            .anyMatch(block -> {
                                if (!city.getChunks().contains(new VirtualChunk(block.getChunk()))) {
                                    player.sendMessage(ChatColor.RED + "Une partie de votre sélection est hors de la ville.");
                                    return true;
                                }

                                Plot check = city.getPlotHere(block.getLocation());
                                if (check != null) {
                                    player.sendMessage(ChatColor.RED + "Une partie de votre sélection fait partie d'une autre parcelle.");
                                    return true;
                                }

                                return false;
                            });

                    if (mustStop)
                        return true;

                    CityPlot plot = new CityPlot();
                    plot.setPlotName(name);
                    plot.setArea(area);

                    city.addPlot(name, plot);
                    player.sendMessage(ChatColor.GREEN + "La parcelle a bien été créée.");
                }
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Sélection invalide : " + e.getMessage());
            }
        }
        return true;
    }
}
