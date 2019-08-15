package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.common.regions.Region;
import net.zyuiop.rpmachine.common.selections.PlayerSelection;
import net.zyuiop.rpmachine.common.selections.Selection;
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
    public boolean run(Player player, String command, String subCommand, String[] args) {
        if (PlayerSelection.getPlayerSelection(player) == null) {
            player.sendMessage(ChatColor.RED + "Vous n'avez sélectionné aucune région.");
            return false;
        } else {
            Selection selection = PlayerSelection.getPlayerSelection(player);

            try {
                Region area = selection.getRegion();
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
                        area.expandY(-255);
                        area.expandY(255);

                        player.sendMessage(ChatColor.RED + "L'option groundtosky n'est plus supportée, utilisez plutôt " + ChatColor.YELLOW + "/sel expand");
                    }

                    // Area check
                    if (plot.checkArea(area, manager, player)) {
                        manager.saveZone(plot);
                        player.sendMessage(ChatColor.GREEN + "Le projet a bien été redéfini.");
                    }
                }
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Sélection invalide : " + e.getMessage());
            }
        }

        return true;
    }
}
