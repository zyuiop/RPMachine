package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.commands.ConfirmationCommand;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.permissions.Permission;
import net.zyuiop.rpmachine.permissions.PermissionTypes;
import net.zyuiop.rpmachine.utils.Messages;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

public class FusionCommand implements CityMemberSubCommand, ConfirmationCommand {
    private final CitiesManager citiesManager;

    public FusionCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<start <ville cible>|accept <ville cible> [nouveau nom]|abort>";
    }

    @Override
    public String getDescription() {
        return "fusionne votre ville avec une autre";
    }

    @Override
    public boolean requiresMayorPrivilege() {
        return true;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        if (args.length < 1 || (!args[0].equalsIgnoreCase("abort") && args.length < 2)) {
            player.sendMessage(ChatColor.RED + "Arguments manquants: " + getUsage());
            return false;
        }

        if (args[0].equalsIgnoreCase("abort")) {
            if (city.getProposedFusion() == null) {
                player.sendMessage(ChatColor.RED + "Vous n'avez pas encore proposé de fusion.");
            } else {
                player.sendMessage(ChatColor.GREEN + "Proposition de fusion refusée !");
            }
        } else {
            City c = citiesManager.getCity(args[1]);
            if (c == null) {
                player.sendMessage(ChatColor.RED + "Cette ville n'existe pas.");
                return false;
            }

            if (args[0].equalsIgnoreCase("accept")) {
                if (c.getProposedFusion() == null || !c.getProposedFusion().equals(city.tag())) {
                    player.sendMessage(ChatColor.RED + "Cette ville ne vous a pas fait d'offre de fusion, ou elle l'a retirée.");
                } else {
                    if (requestConfirm(player,
                            ChatColor.YELLOW + "Voulez vous vraiment absorber le territoire et les parcelles de " + c.shortDisplayable() + ChatColor.YELLOW + " ?",
                            command + " " + subcommand,
                            args)) {

                        if (args.length >= 3) {
                            String cityName = args[2];
                            if (!cityName.matches("^[a-zA-Z0-9]{3,16}$")) {
                                player.sendMessage(ChatColor.RED + "Le nom de votre ville n'est pas valide (3 à 15 caractères alphanumériques)");
                                return true;
                            } else if (citiesManager.getCity(cityName) != null) {
                                player.sendMessage(ChatColor.RED + "Une ville de ce nom existe déjà.");
                                return true;
                            }

                            citiesManager.move(city, cityName);
                            player.sendMessage(ChatColor.GREEN + Symbols.OK + ChatColor.GRAY + " Ville renommée");
                        }

                        for (VirtualChunk vc : c.getChunks())
                            city.addChunk(vc);

                        player.sendMessage(ChatColor.GREEN + Symbols.OK + ChatColor.GRAY + " Chunks importés");

                        for (Map.Entry<String, Plot> p : c.getPlots().entrySet()) {
                            String name = p.getKey();

                            if (city.getPlots().containsKey(name)) {
                                name = c.getCityName() + "_" + name;
                            }

                            if (city.getPlots().containsKey(name)) {
                                name = name + new Random().nextInt(50000);
                            }

                            Plot plot = p.getValue();
                            plot.setPlotName(name);
                            city.getPlots().put(name, plot);
                        }

                        player.sendMessage(ChatColor.GREEN + Symbols.OK + ChatColor.GRAY + " Plots importés");

                        HashSet<UUID> inhabitants = new HashSet<>(c.getInhabitants());
                        c.getInhabitants().clear();
                        city.getInhabitants().addAll(inhabitants);
                        player.sendMessage(ChatColor.GREEN + Symbols.OK + ChatColor.GRAY + " Citoyens importés");

                        HashMap<UUID, Set<Permission>> councils = new HashMap<>(c.getFullCouncils());
                        c.getFullCouncils().clear();
                        city.getFullCouncils().putAll(councils);
                        player.sendMessage(ChatColor.GREEN + Symbols.OK + ChatColor.GRAY + " Conseillers importés");

                        city.addCouncil(c.getMayor());
                        for (PermissionTypes tp : PermissionTypes.values())
                            for (Permission p : tp.members())
                                city.addPermission(c.getMayor(), p);
                        player.sendMessage(ChatColor.GREEN + Symbols.OK + ChatColor.GRAY + " Maire importé");

                        for (Map.Entry<String, Double> tp : c.getTaxesToPay().entrySet()) {
                            if (city.getTaxesToPay().containsKey(tp.getKey())) {
                                city.getTaxesToPay().put(tp.getKey(), tp.getValue() + city.getTaxesToPay().get(tp.getKey()));
                            } else {
                                city.getTaxesToPay().put(tp.getKey(), tp.getValue());
                            }

                            LegalEntity le = LegalEntity.getEntity(tp.getKey());
                            le.setUnpaidTaxes(c.getCityName(), 0D);
                            le.setUnpaidTaxes(city.getCityName(), city.getTaxesToPay().get(tp.getKey()));
                        }

                        for (Map.Entry<String, Double> tp : c.getUnpaidTaxes().entrySet()) {
                            if (city.getUnpaidTaxes(tp.getKey()) > 0D) {
                                city.setUnpaidTaxes(tp.getKey(), city.getUnpaidTaxes(tp.getKey()) + tp.getValue());
                            } else {
                                city.setUnpaidTaxes(tp.getKey(), tp.getValue());
                            }

                            City third = citiesManager.getCity(tp.getKey());
                            third.getTaxesToPay().remove(c.tag());
                            third.getTaxesToPay().put(c.tag(), city.getUnpaidTaxes(tp.getKey()));
                        }

                        player.sendMessage(ChatColor.GREEN + Symbols.OK + ChatColor.GRAY + " Taxes en retard importées");

                        citiesManager.removeCity(c);

                        player.sendMessage(ChatColor.GREEN + Symbols.OK + " Fusion terminée !");


                    }
                }
            } else if (args[0].equalsIgnoreCase("start")) {
                if (city.getProposedFusion() == null) {

                    if (requestConfirm(player,
                            ChatColor.YELLOW + "Voulez vous vraiment céder le territoire et les parcelles de votre ville à " + c.shortDisplayable() + ChatColor.YELLOW + " ? Votre rang de maire sera remplacé par un rang de conseiller, et vos conseillers deviendront des conseillers de la nouvelle ville.",
                            command + " " + subcommand,
                            args)) {

                        city.setProposedFusion(c.tag());
                        Messages.sendMessage(city, ChatColor.GREEN + "Une proposition de fusion de ville a été envoyée à " + c.shortDisplayable());

                        Messages.sendMessage(c, ChatColor.YELLOW + "Vous avez reçu une proposition de fusion de " + city.displayable() + ChatColor.YELLOW + ".");
                        Messages.sendMessage(c, ChatColor.GRAY + "Si vous acceptez la fusion, vous intègrerez toutes les parcelles et citoyens de cette ville dans votre ville, son maire deviendra automatiquement conseiller, et tous les conseillers deviendront également conseillers.");
                        Messages.sendMessage(c, ChatColor.YELLOW + "Pour acceptez, demandez au maire de lancer " + ChatColor.GOLD + "/" + command + " accept " + city.getCityName());
                        Messages.sendMessage(c, ChatColor.GRAY + "Vous pouvez aussi changer le nom de la nouvelle ville via " + ChatColor.YELLOW + "/" + command + " accept " + city.getCityName() + " <nouveau nom>");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Vous avez déjà lancé une proposition de fusion. Merci de la suspendre avec " + ChatColor.DARK_RED + "/" + command + " " + subcommand + " abort");
                }
            }
        }
        return true;
    }
}
