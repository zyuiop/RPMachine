package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.CityFloor;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FloorsCommand extends AbstractCommand implements SubCommand {
    // Command registered as a subcommand -- but the instanciation creates a command as well
    public FloorsCommand() {
        super("floors", null, "paliers");
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        return run(player, args);
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
    public boolean run(Player commandSender, String[] args) {
        commandSender.sendMessage(ChatColor.GOLD + " -----[ Paliers de Villes ] -----");
        commandSender.sendMessage(ChatColor.YELLOW + "Voici la liste des paliers disponibles :");
        for (CityFloor floor : RPMachine.getInstance().getCitiesManager().getFloors()) {
            commandSender.sendMessage(ChatColor.YELLOW + " - " + ChatColor.GOLD + floor.getName() + ChatColor.YELLOW + ", débloqué à " + ChatColor.GOLD + floor.getInhabitants() + " habitants.");
            commandSender.sendMessage(ChatColor.DARK_AQUA + "Prix par chunk : " + ChatColor.AQUA + floor.getChunkPrice() + " " + Economy.getCurrencyName());
            commandSender.sendMessage(ChatColor.DARK_AQUA + "Impôt maximal : " + ChatColor.AQUA + floor.getMaxtaxes() + " " + Economy.getCurrencyName());
            commandSender.sendMessage(ChatColor.DARK_AQUA + "Taille maximale : " + ChatColor.AQUA + floor.getMaxsurface() + " Chunks");
        }
        return true;
    }
}
