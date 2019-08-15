package net.zyuiop.rpmachine.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.common.VirtualLocation;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CommandHome extends AbstractCommand {
    private final boolean fromCityOnly;

    public CommandHome() {
        super("home", null);
        fromCityOnly = RPMachine.getInstance().getConfig().getBoolean("home.fromCityOnly", false);
    }

    @Override
    public boolean onPlayerCommand(Player player, String s, String[] strings) {
        if (fromCityOnly && !RPMachine.getInstance().getCitiesManager().checkCityTeleport(player)) {
            return true;
        }

        PlayerData data = RPMachine.database().getPlayerData(player.getUniqueId());
        VirtualLocation loc = data.getHome();
        if (loc == null)
            player.sendMessage(ChatColor.RED + "Vous n'avez pas défini de domicile.");
        else {
            Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> {
                Location tp = loc.getLocation();
                if (!tp.getChunk().isLoaded())
                    tp.getChunk().load();
                player.teleport(tp);
                ReflectionUtils.getVersion().playEndermanTeleport(tp, player);
                player.sendMessage(ChatColor.GOLD + "Vous avez été téléporté !");
            });
        }
        return true;
    }
}
