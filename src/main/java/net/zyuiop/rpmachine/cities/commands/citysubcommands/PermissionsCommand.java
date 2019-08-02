package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.CitiesPermGui;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.UUID;

public class PermissionsCommand implements CityMemberSubCommand {
    @Override
    public Permission requiresPermission() {
        return CityPermissions.CHANGE_COUNCIL_PERMS;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String[] args) {
        if (args.length < 1) {
            player.sendMessage("Pseudo manquant.");
            return false;
        }

        String name = args[0];
        UUID id = RPMachine.database().getUUIDTranslator().getUUID(args[0], true);

        if (!city.getCouncils().contains(id)) {
            player.sendMessage(ChatColor.RED + "Cette personne n'est pas encore un conseiller. Nommez le conseiller pour poursuivre.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Modification des permissions de " + ChatColor.YELLOW + name);
        new CitiesPermGui(player, id, name, city).open();
            return true;

    }

    @Override
    public String getUsage() {
        return "<pseudo>>";
    }

    @Override
    public String getDescription() {
        return "modifie les permissions du conseiller";
    }
}
