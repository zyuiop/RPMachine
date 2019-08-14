package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CityFloor;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FloorsCommand extends AbstractCommand implements SubCommand {
    // Command registered as a subcommand -- but the instanciation creates a command as well
    public FloorsCommand() {
        super("floors", null, "paliers");
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        return run(player, command, "", args);
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "liste les paliers de villes";
    }

    @Override
    public boolean run(Player commandSender, String command, String subCommand, String[] args) {
        commandSender.sendMessage(ChatColor.GOLD + " -----[ Paliers de Villes ] -----");
        commandSender.sendMessage(ChatColor.YELLOW + "Voici la liste des paliers disponibles :");
        for (CityFloor floor : RPMachine.getInstance().getCitiesManager().getFloors()) {
            commandSender.sendMessage(ChatColor.YELLOW + " - " + ChatColor.GOLD + floor.getName() + ChatColor.YELLOW + ", débloqué à " + ChatColor.GOLD + floor.getInhabitants() + " habitants.");
            commandSender.sendMessage(ChatColor.DARK_AQUA + "Prix par chunk : " + ChatColor.AQUA + floor.getChunkPrice() + " " + RPMachine.getCurrencyName());
            commandSender.sendMessage(ChatColor.DARK_AQUA + "Impôt max : " + ChatColor.AQUA + floor.getMaxtaxes() + " " + RPMachine.getCurrencyName());
            commandSender.sendMessage(ChatColor.DARK_AQUA + "Taille max : " + ChatColor.AQUA + floor.getMaxsurface() + " " + RPMachine.getCurrencyName());
            if (RPMachine.isTpEnabled())
                commandSender.sendMessage(ChatColor.DARK_AQUA + "Taxe TP max : " + ChatColor.AQUA + floor.getMaxTpTax() + " " + RPMachine.getCurrencyName());
        }
        return true;
    }
}
