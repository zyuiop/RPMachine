package net.zyuiop.rpmachine.cities.commands.citysubcommands.params;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

public class SetChatColorCommand implements CityMemberSubCommand {

    private final CitiesManager citiesManager;

    public SetChatColorCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "[couleur dans le chat]";
    }

    @Override
    public String getDescription() {
        return "modifie la couleur du tag de la ville dans le chat";
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.SET_CHAT_COLOR;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Couleur dans le chat actuelle : " + city.getChatColor() + city.getChatColor().name());
            player.sendMessage(ChatColor.YELLOW + "Couleurs disponibles : " + CitiesManager.ALLOWED_COLORS.stream().map(c -> c + c.name()).collect(Collectors.joining(ChatColor.RESET + ", ")));
            return true;
        } else {
            try {
                ChatColor value = ChatColor.valueOf(args[0]);
                if (!CitiesManager.ALLOWED_COLORS.contains(value)) {
                    throw new IllegalArgumentException();
                }
                city.setChatColor(value);
                city.save();
                player.sendMessage(ChatColor.GREEN + "La couleur dans le chat est désormais " + city.getChatColor() + city.getChatColor().name());
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "Couleurs disponibles : " + CitiesManager.ALLOWED_COLORS.stream().map(c -> c + c.name()).collect(Collectors.joining(ChatColor.RESET + ", ")));
            }
            return true;
        }
    }
}
