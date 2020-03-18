package net.zyuiop.rpmachine.cities.commands.citysubcommands.params;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.politics.PoliticalSystems;
import net.zyuiop.rpmachine.cities.voting.ChangePoliticalSystemVoteHook;
import net.zyuiop.rpmachine.cities.voting.Votation;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetPoliticalSystemCommand implements CityMemberSubCommand {

    @Override
    public String getUsage() {
        return "[système politique]";
    }

    @Override
    public String getDescription() {
        return "modifie le système politique de la ville";
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.SET_POLITICAL_SYSTEM;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Système politique actuel : " + ChatColor.GOLD + city.getPoliticalSystem().getName());
            player.sendMessage(ChatColor.YELLOW + "Systèmes disponibles : " + Arrays.stream(PoliticalSystems.values()).map(Enum::name).collect(Collectors.joining(", ")));
            return true;
        } else {
            PoliticalSystems value;
            try {
                value = PoliticalSystems.valueOf(args[0]);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Systèmes disponibles : " + Arrays.stream(PoliticalSystems.values()).map(Enum::name).collect(Collectors.joining(", ")));
                return true;
            }
            if (city.getPoliticalSystem().isParameterRestricted("politicalSystem")) {
                player.sendMessage(ChatColor.GREEN + "Le changement de système a été soumis au vote !");
                RPMachine.getInstance().getVotationsManager().createVotation(Votation.yesNoVotation(city.getCityName(), "Voulez vous changer de système politique vers " + value.instance.getName() + " ? (plus d'infos avec /ps)", System.currentTimeMillis() + 48 * 3600 * 1000L, new ChangePoliticalSystemVoteHook(value)));
            } else {
                city.setPoliticalSystem(value);
            }

            return true;
        }
    }


    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return args.length > 0 ? tabCompleteHelper(args[0], Arrays.stream(PoliticalSystems.values()).map(Enum::name).collect(Collectors.toList())) : null;
    }
}
