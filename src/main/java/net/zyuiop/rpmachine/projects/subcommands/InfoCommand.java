package net.zyuiop.rpmachine.projects.subcommands;

import joptsimple.internal.Strings;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class InfoCommand implements SubCommand {
    private final ProjectsManager manager;

    public InfoCommand(ProjectsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "affiche des informations sur le projet dans lequel vous vous trouvez";
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {
        Project plot = manager.getZoneHere(player.getLocation());
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "Vous ne vous trouvez pas dans un projet.");
        } else {
            player.sendMessage(ChatColor.GOLD + "-----[ Informations Projet ]-----");
            player.sendMessage(ChatColor.YELLOW + "Nom : " + plot.getPlotName());
            player.sendMessage(ChatColor.YELLOW + "Surface : " + plot.getArea().computeArea() + " blocs");
            LegalEntity proprio = plot.owner();
            if (proprio == null) {
                player.sendMessage(ChatColor.YELLOW + "Propriétaire : " + ChatColor.RED + "Aucun");
            } else {
                String name = proprio.displayable();
                if (name == null) {
                    player.sendMessage(ChatColor.YELLOW + "Propriétaire : " + ChatColor.GOLD + "Inconnu");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Propriétaire : " + ChatColor.GREEN + name);
                }
            }

            ArrayList<String> members = new ArrayList<>();
            for (UUID id : plot.getPlotMembers()) {
                String name = RPMachine.database().getUUIDTranslator().getName(id);
                if (name != null)
                    members.add(name);
            }

            if (members.size() > 0) {
                String mem = Strings.join(members, ", ");
                player.sendMessage(ChatColor.YELLOW + "Membres : " + mem);
            }
        }

        return true;
    }
}
