package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AllowCreateCityCommand extends AbstractCommand implements SubCommand {
    public AllowCreateCityCommand() {
        super("allowcreatecity", "admin.setallowcreate");
    }

    @Override
    public String getUsage() {
        return "<pseudo>";
    }

    @Override
    public String getDescription() {
        return "autorise un joueur à créer une ville (valable 3h)";
    }

    @Override
    public boolean canUse(Player player) {
        return player.hasPermission("admin.setallowcreate");
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {
        return runCS(player, command, subCommand, args);
    }

    private boolean runCS(CommandSender player, String command, String subCommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Pseudo manquant.");
            return true;
        }

        UUID targetId = RPMachine.getInstance().getDatabaseManager().getUUIDTranslator().getUUID(args[0]);
        if (targetId == null) {
            player.sendMessage(ChatColor.RED + "Pseudo introuvable.");
            return true;
        }

        PlayerData data = RPMachine.getInstance().getDatabaseManager().getPlayerData(targetId);
        data.setAttribute(CreateCityCommand.ALLOW_CREATE_ATTR_KEY, System.currentTimeMillis() + (3600L * 1000L * 3L));

        player.sendMessage(ChatColor.GREEN + "Le joueur a désormais le droit de créer une ville pendant les trois prochaines heures.");
        Messages.sendMessage(data, ChatColor.GREEN + "La permission de créer une ville vous a été accordée pour les trois prochaines heures.");
        Messages.sendMessage(data, ChatColor.GRAY + "Rendez vous sur le chunk qui vous intéresse et utilisez " + ChatColor.YELLOW + "/city create <nom>" + ChatColor.GRAY + " !");
        return true;
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        return runCS(player, command, "", args);
    }

    @Override
    protected boolean onNonPlayerCommand(CommandSender player, String command, String[] args) {
        return runCS(player, command, "", args);
    }
}
