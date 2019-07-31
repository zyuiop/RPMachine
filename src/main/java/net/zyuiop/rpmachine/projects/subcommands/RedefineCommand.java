package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.common.Area;
import net.zyuiop.rpmachine.common.Selection;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RedefineCommand implements SubCommand {
    private final ProjectsManager manager;

    public RedefineCommand(ProjectsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getUsage() {
        return "<nom> [groundtosky]";
    }

    @Override
    public String getDescription() {
        return "redéfinit le projet avec la sélection actuelle";
    }

    @Override
    public boolean canUse(Player player) {
        return player.hasPermission("zones.redefine");
    }

    @Override
    public boolean run(Player player, String[] args) {
        if (RPMachine.getInstance().getSelectionManager().getSelection(player.getUniqueId()) == null) {
            player.sendMessage(ChatColor.RED + "Vous n'avez sélectionné aucune région.");
        } else {
            Selection selection = RPMachine.getInstance().getSelectionManager().getSelection(player.getUniqueId());
            if (selection.getLocation1() == null || selection.getLocation2() == null) {
                player.sendMessage(ChatColor.RED + "Votre sélection n'est pas complète.");
            } else {
                Area area = selection.getArea();
                if (args.length < 1) {
                    player.sendMessage(ChatColor.RED + "Syntaxe invalide : /project redefine " + getUsage());
                } else {
                    String name = args[0];
                    Project plot = manager.getZone(name);
                    if (plot == null) {
                        player.sendMessage(ChatColor.RED + "Il n'existe aucune zone de ce nom. Merci d'en créer une.");
                        return true;
                    }

                    if (args.length > 1 && args[1].equalsIgnoreCase("groundtosky")) {
                        area.setMaxY(254);
                        area.setMinY(1);
                    }

                    // Area check
                    if (plot.checkArea(area, manager, player)) {
                        manager.saveZone(plot);
                        player.sendMessage(ChatColor.GREEN + "Le projet a bien été redéfinie.");
                    }
                }
            }
        }

        return true;
    }
}
