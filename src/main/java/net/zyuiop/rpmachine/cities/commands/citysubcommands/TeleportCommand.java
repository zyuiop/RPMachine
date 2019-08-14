package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.commands.ConfirmationCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.reflection.ReflectionUtils;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportCommand implements SubCommand, ConfirmationCommand {
    private final CitiesManager citiesManager;
    private final double price;
    private final boolean fromCityOnly;

    public TeleportCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;

        price = RPMachine.getInstance().getConfig().getDouble("citytp.cost", 1D);
        fromCityOnly = RPMachine.getInstance().getConfig().getBoolean("citytp.fromCityOnly", false);
    }


    @Override
    public String getUsage() {
        return "[ville]";
    }

    @Override
    public boolean canUse(Player player) {
        if (!RPMachine.isTpEnabled())
            return player.hasPermission("admin.teleport");

        return true;
    }

    @Override
    public String getDescription() {
        return "vous téléporte dans votre ville ou la ville fournie (contre " + price + " " + RPMachine.getCurrencyName() + ")";
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {
        if (fromCityOnly && !RPMachine.getInstance().getCitiesManager().checkCityTeleport(player)) {
            return true;
        }

        City playerCity = citiesManager.getPlayerCity(player.getUniqueId());
        City target;
        int price = 0;
        if (args.length > 0) {
            target = citiesManager.getCity(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Cette ville n'existe pas.");
                return true;
            } else if (target.isRequireInvite() && !target.getInvitedUsers().contains(player.getUniqueId()) && (playerCity == null || !playerCity.getCityName().equalsIgnoreCase(target.getCityName()))) {
                player.sendMessage(ChatColor.RED + "Cette ville est privée.");
                return true;
            } else if (playerCity == null || !playerCity.getCityName().equalsIgnoreCase(target.getCityName())) {
                price = target.getTpTax();
            }
        } else {
            target = playerCity;
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Vous ne faites partie d'aucune ville.");
                return true;
            }
        }

        VirtualLocation vspawn = target.getSpawn();
        if (vspawn == null) {
            player.sendMessage(ChatColor.RED + "Cette ville ne dispose d'aucun point de spawn.");
            return true;
        }

        Location spawn = vspawn.getLocation();
        if (!spawn.getChunk().isLoaded())
            spawn.getChunk().load();

        if (price > 0) {
            if (requestConfirm(player, ChatColor.YELLOW + "La téléportation vers " + target.shortDisplayable() +
                    ChatColor.YELLOW + " coûte " + ChatColor.GOLD + price + " " + RPMachine.getCurrencyName() +
                    ChatColor.YELLOW + ".", command + " " + subCommand, args)) {

                if (!RPMachine.database().getPlayerData(player).transfer(price, target)) {
                    Messages.notEnoughMoney(player, price);
                } else {
                    Messages.debit(player, price, "téléportation vers " + target.getCityName());
                    Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> player.teleport(spawn));
                    ReflectionUtils.getVersion().playEndermanTeleport(spawn, player);
                }
            }
        } else {
            Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> player.teleport(spawn));
            ReflectionUtils.getVersion().playEndermanTeleport(spawn, player);
            player.sendMessage(ChatColor.GOLD + "Vous avez été téléporté.");
        }
        return true;
    }
}
