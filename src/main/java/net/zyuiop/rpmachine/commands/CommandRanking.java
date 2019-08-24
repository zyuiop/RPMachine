package net.zyuiop.rpmachine.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Louis Vialar
 */
public class CommandRanking extends AbstractCommand {
    public CommandRanking() {
        super("ranking", null, "ranks");
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        List<PlayerData> data = new ArrayList<>(RPMachine.getInstance().getDatabaseManager().getActivePlayers());
        data.sort(Comparator.comparing(PlayerData::getBalance).reversed());

        PlayerData own = RPMachine.getInstance().getDatabaseManager().getPlayerData(player);
        int ownRank = data.indexOf(own);

        for (int i = 0; i < 10 && i < data.size() ; ++i) {
            player.sendMessage("#" + (i + 1) + " " + ChatColor.YELLOW + data.get(i).getName() + ChatColor.WHITE + " : " + ChatColor.AQUA + String.format("%.2f", data.get(i).getBalance()) + RPMachine.getCurrencyName());
        }

        player.sendMessage(ChatColor.YELLOW + "Votre classement : " + ChatColor.AQUA + "#" + (ownRank + 1));

        return true;
    }
}
