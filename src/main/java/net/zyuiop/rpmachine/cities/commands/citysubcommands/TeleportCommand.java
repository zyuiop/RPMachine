package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportCommand implements SubCommand {
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
    public String getDescription() {
        return "vous téléporte dans votre ville ou la ville fournie (contre " + price + " " + EconomyManager.getMoneyName() + ")";
    }

    @Override
    public boolean run(Player player, String[] args) {
        if (fromCityOnly && !RPMachine.getInstance().getCitiesManager().checkCityTeleport(player)) {
            return true;
        }

        City playerCity = citiesManager.getPlayerCity(player.getUniqueId());
        City target;
        boolean pay = false;
        if (args.length > 0) {
            target = citiesManager.getCity(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Cette ville n'existe pas.");
                return true;
            } else if (target.isRequireInvite() && !target.getInvitedUsers().contains(player.getUniqueId()) && (playerCity == null || !playerCity.getCityName().equalsIgnoreCase(target.getCityName()))) {
                player.sendMessage(ChatColor.RED + "Cette ville est privée.");
                return true;
            } else if (playerCity == null || !playerCity.getCityName().equalsIgnoreCase(target.getCityName())) {
                pay = true;
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

        if (pay) {
            RPMachine.getInstance().getEconomyManager().withdrawMoneyWithBalanceCheck(player.getUniqueId(), price, (newAmount, difference) -> {
                if (!difference) {
                    player.sendMessage(ChatColor.RED + "Vous n'avez pas assez d'argent pour faire cela.");
                } else {
                    target.setMoney(target.getMoney() + price);
                    citiesManager.saveCity(target);
                    Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> player.teleport(spawn));
                    ReflectionUtils.getVersion().playEndermanTeleport(spawn, player);
                    player.sendMessage(ChatColor.GOLD + "Vous avez été téléporté et " + price + " " + EconomyManager.getMoneyName() + " vous a été débité.");
                }
            });
        } else {
            Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> player.teleport(spawn));
            ReflectionUtils.getVersion().playEndermanTeleport(spawn, player);
            player.sendMessage(ChatColor.GOLD + "Vous avez été téléporté.");
        }
        return true;
    }
}
