package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.CityFloor;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class ClaimCommand implements CityMemberSubCommand {

    private final CitiesManager citiesManager;

    public ClaimCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "ajoute le chunk actuel à votre ville";
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.EXPAND_CITY;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String[] args) {
        if (!player.getLocation().getWorld().getName().equals("world")) {
            player.sendMessage(ChatColor.RED + "Il est impossible d'agrandir votre ville sur cette carte.");
            return false;
        }

        CityFloor floor = citiesManager.getFloor(city);
        Chunk chunk = player.getLocation().getChunk();
        if (citiesManager.getCityHere(chunk) != null) {
            player.sendMessage(ChatColor.RED + "Ce chunk appartient déjà a une ville.");
            return false;
        } else if (RPMachine.getInstance().getProjectsManager().getZonesHere(chunk).size() > 0) {
            player.sendMessage(ChatColor.RED + "Il y a des projets de la Confédération dans ce chunk.");
            return false;
        } else if (!city.isAdjacent(chunk)) {
            player.sendMessage(ChatColor.RED + "Ce chunk n'est pas adjacent à votre ville.");
            return false;
        } else if (city.getBalance() < floor.getChunkPrice()) {
            player.sendMessage(ChatColor.RED + "Votre ville ne dispose pas d'assez d'argent pour faire cela. Il lui faut " + floor.getChunkPrice() + " " + RPMachine.getCurrencyName() + " au minimum.");
            return false;
        } else if (city.getChunks().size() >= floor.getMaxsurface()) {
            player.sendMessage(ChatColor.RED + "Votre ville a atteind sa taille maximale.");
            return false;
        } else if (args.length >= 1 && args[0].equals("confirm")) {
            city.getChunks().add(new VirtualChunk(chunk));
            city.withdrawMoney(floor.getChunkPrice());
            citiesManager.saveCity(city);
            player.sendMessage(ChatColor.GREEN + "Votre ville a bien été agrandie sur ce terrain !");
            return true;
        } else {
            player.sendMessage(ChatColor.GOLD + "Êtes vous certain de vouloir acheter ce chunk ? Cela vous coûtera " + ChatColor.YELLOW + floor.getChunkPrice() + " " + RPMachine.getCurrencyName());
            player.sendMessage(ChatColor.GOLD + "Tapez " + ChatColor.YELLOW + "/city claim confirm" + ChatColor.GOLD + " pour valider.");
            return true;
        }
    }
}
