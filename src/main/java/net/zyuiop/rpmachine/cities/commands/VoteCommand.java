package net.zyuiop.rpmachine.cities.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.cities.CityFloor;
import net.zyuiop.rpmachine.cities.voting.VotationsManager;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class VoteCommand extends AbstractCommand implements SubCommand {
    private final VotationsManager votations;
    private final CitiesManager cities;

    // Command registered as a subcommand -- but the instanciation creates a command as well
    public VoteCommand(VotationsManager votations, CitiesManager cities) {
        super("vote", null, "v");
        this.votations = votations;
        this.cities = cities;
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        return run(player, command, "", args);
    }

    @Override
    public String getUsage() {
        return "[id] [option]";
    }

    @Override
    public String getDescription() {
        return "liste les votations de votre ville, ou vote";
    }

    @Override
    public boolean run(Player commandSender, String command, String subCommand, String[] args) {
        City city = cities.getPlayerCity(commandSender);

        if (city == null) {
            commandSender.sendMessage(ChatColor.RED + "Vous devez être citoyen d'une ville pour voter.");
            return false;
        }

        if (args.length == 0) {
            commandSender.sendMessage(ChatColor.YELLOW + "Voici la liste des votations en cours à " + city.getChatColor() + city.getCityName());

            votations.getVotationsIn(city.getCityName()).forEach(v -> v.printShort(commandSender));
        } else if (args.length == 1) {
            try {
                var id = Integer.parseInt(args[0]);
                var votation = votations.getCurrentVotationById(id);

                if (votation == null) {
                    commandSender.sendMessage(ChatColor.RED + "Cette votation n'existe pas ou est déjà terminée.");
                } else if (!votation.hasVoteRight(commandSender.getUniqueId())) {
                    commandSender.sendMessage(ChatColor.RED + "Vous ne disposez pas du droit de vote pour cette votation.");
                } else {
                    votation.print(commandSender);
                }
            } catch (NumberFormatException e) {
                commandSender.sendMessage(ChatColor.RED + "Argument incorrect : /vote <numéro de votation>.");
            }
        } else {
            try {
                var id = Integer.parseInt(args[0]);
                var votation = votations.getCurrentVotationById(id);
                var vote = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

                if (votation == null) {
                    commandSender.sendMessage(ChatColor.RED + "Cette votation n'existe pas ou est déjà terminée.");
                } else {
                    votation.vote(commandSender, vote);
                    votations.save(votation);
                }
            } catch (NumberFormatException e) {
                commandSender.sendMessage(ChatColor.RED + "Argument incorrect : /vote <numéro de votation> <vote>.");
            }
        }

        return true;
    }
}
