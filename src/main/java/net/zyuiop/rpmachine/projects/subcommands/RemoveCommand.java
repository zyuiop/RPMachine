package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.shops.types.AbstractShopSign;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RemoveCommand implements SubCommand {
    private final ProjectsManager manager;

    public RemoveCommand(ProjectsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getUsage() {
        return "<nom>";
    }

    @Override
    public String getDescription() {
        return "supprime le projet";
    }

    @Override
    public boolean canUse(Player player) {
        return player.hasPermission("zones.remove");
    }

    @Override
    public boolean run(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Argument manquant.");
            return false;
        } else {
            Project plot = manager.getZone(args[0]);
            if (plot == null) {
                player.sendMessage(ChatColor.RED + "Il n'existe aucun projet de ce nom.");
                return true;
            }

            RPMachine.getInstance().getShopsManager().getShops(plot).forEach(AbstractShopSign::breakSign);

            manager.removeZone(plot);
            player.sendMessage(ChatColor.GREEN + "Le projet a bien été supprimé.");
            return true;
        }

    }
}
