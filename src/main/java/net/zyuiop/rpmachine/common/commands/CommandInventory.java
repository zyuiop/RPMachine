package net.zyuiop.rpmachine.common.commands;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CommandInventory extends AbstractCommand {
    public CommandInventory() {
        super("invsee", "rp.inventory", "endsee");
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] strings) {
        if (strings.length < 1) {
            player.sendMessage(ChatColor.RED + "Erreur : pseudo manquant.");
            return true;
        }

        Player target = Bukkit.getPlayerExact(strings[0]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Joueur introuvable.");
            return true;
        }
        player.openInventory(command.toLowerCase().equalsIgnoreCase("endsee") ? target.getEnderChest() : target.getInventory());
        return true;
    }
}
