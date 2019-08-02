package net.zyuiop.rpmachine.multiverse;

import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.common.Area;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * @author Louis Vialar
 */
public class ForceRegenCommand extends AbstractCommand {
    private final MultiverseManager manager;

    protected ForceRegenCommand(MultiverseManager manager) {
        super("forceregen", "admin.forceregen");
        this.manager = manager;
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Utilisation : /" + command + " <monde cible>");
            return false;
        }

        boolean success = manager.forceRegen(args[0]) > 0;

        if (success) {
            player.sendMessage(ChatColor.GREEN + "Le monde " + ChatColor.YELLOW + args[0] + ChatColor.GREEN + " sera regénéré au prochain reboot.");
        } else {
            player.sendMessage(ChatColor.RED + "Le monde ciblé n'est pas un multivers automatique ou n'existe pas.");
        }
        return true;
    }
}
