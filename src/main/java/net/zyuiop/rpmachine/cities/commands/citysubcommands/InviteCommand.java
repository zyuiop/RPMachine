package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.UUID;

public class InviteCommand implements CityMemberSubCommand {

    private final CitiesManager citiesManager;

    public InviteCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<pseudo>";
    }

    @Override
    public String getDescription() {
        return "invite un joueur dans votre ville";
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.INVITE_MEMBER;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Utilisation incorrecte : /city invite " + getUsage());
            return false;
        } else {
            if (!city.isRequireInvite()) {
                player.sendMessage(ChatColor.RED + "Votre ville ne nécessite pas d'invitation pour être rejointe.");
                return false;
            }

            String name = args[0];
            UUID id = RPMachine.database().getUUIDTranslator().getUUID(name);
            if (id == null) {
                player.sendMessage(ChatColor.RED + "Ce joueur n'existe pas.");
            } else if (city.getInvitedUsers().contains(id)) {
                player.sendMessage(ChatColor.RED + "Ce joueur est déjà invité dans votre ville.");
            } else if (city.getInhabitants().contains(id)) {
                player.sendMessage(ChatColor.RED + "Ce joueur habite déjà votre ville.");
            } else {
                city.getInvitedUsers().add(id);
                citiesManager.saveCity(city);
                Player target = Bukkit.getPlayer(id);
                if (target != null) {
                    target.sendMessage(ChatColor.GOLD + "Vous avez été invité à rejoindre la ville " + ChatColor.YELLOW + city.getCityName() + ChatColor.GOLD + " par " + ChatColor.YELLOW + player.getName());
                    target.sendMessage(ChatColor.GOLD + "Pour rejoindre cette ville, utilisez " + ChatColor.YELLOW + "/city join " + city.getCityName());
                }

                player.sendMessage(ChatColor.GREEN + "Le joueur a bien été invité.");
                if (citiesManager.getPlayerCity(id) != null)
                    player.sendMessage(ChatColor.GOLD + "Le joueur étant déjà membre d'une ville, il est probable qu'il n'accepte pas votre invitation.");
            }
            return true;
        }
    }
}
