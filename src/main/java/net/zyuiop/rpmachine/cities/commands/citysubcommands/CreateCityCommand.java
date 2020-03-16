package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.commands.ConfirmationCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CreateCityCommand implements SubCommand, ConfirmationCommand {
    public static final String ALLOW_CREATE_ATTR_KEY = "city_create_allowed";

    private final CitiesManager citiesManager;

    public CreateCityCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<nom> <private|public>";
    }

    @Override
    public String getDescription() {
        return "crée une ville (coût actuel: " + citiesManager.getCreationPrice() + " " + RPMachine.getCurrencyName() + ")";
    }

    @Override
    public boolean canUse(Player player) {
        PlayerData data = RPMachine.getInstance().getDatabaseManager().getPlayerData(player);
        return player.hasPermission("rp.createcity")
                && citiesManager.getPlayerCity(player) == null
                && data.hasAttribute(ALLOW_CREATE_ATTR_KEY)
                && data.<Long> getAttribute(ALLOW_CREATE_ATTR_KEY) > System.currentTimeMillis();
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] strings) {
        if (strings.length >= 2) {
            String cityName = strings[0];
            String type = strings[1];
            if (!cityName.matches("^[a-zA-Z0-9]{3,16}$")) {
                player.sendMessage(ChatColor.RED + "Le nom de votre ville n'est pas valide (3 à 15 caractères alphanumériques)");
            } else if (!type.equalsIgnoreCase("private") && !type.equalsIgnoreCase("public")) {
                player.sendMessage(ChatColor.RED + "Le type de ville ne peut être que PRIVATE ou PUBLIC.");
            } else if (citiesManager.getCityHere(player.getLocation().getChunk()) != null) {
                player.sendMessage(ChatColor.RED + "Il y a déjà une ville sur ce chunk.");
            } else if (RPMachine.getInstance().getProjectsManager().getZonesHere(player.getLocation().getChunk()).size() > 0) {
                player.sendMessage(ChatColor.RED + "Il y a des projets de la Confédération dans ce chunk.");
                return false;
            } else if (citiesManager.getCity(cityName) != null) {
                player.sendMessage(ChatColor.RED + "Une ville de ce nom existe déjà.");
            } else {
                String confirmMsg = ChatColor.GOLD + "Voulez vous vraiment créer une ville ici ? Cela vous coûtera " + ChatColor.YELLOW + citiesManager.getCreationPrice() + " " + RPMachine.getCurrencyName();
                if (requestConfirm(player, confirmMsg, command + " " + subCommand, strings)) {
                    PlayerData data = RPMachine.database().getPlayerData(player);
                    double amt = citiesManager.getCreationPrice();

                    if (!data.withdrawMoney(amt)) {
                        Messages.notEnoughMoney(player, amt);
                    } else {
                        City city = new City();
                        city.setCityName(cityName);
                        city.addChunk(new VirtualChunk(player.getLocation().getChunk()));
                        city.addInhabitant(player.getUniqueId());
                        city.setMayor(player.getUniqueId());
                        city.setRequireInvite(type.equalsIgnoreCase("private"));
                        city.setTaxes(0.0);
                        city.setMayorWage(0.0);
                        city.setTpTax(1);
                        city.setPlotSellTaxRate(0.2);
                        city.setJoinTax(0);
                        city.setSpawn(null);
                        boolean result = citiesManager.createCity(city);
                        if (result) {
                            player.sendMessage(ChatColor.GOLD + "Vous créez une ville sur ce chunk.");
                            data.setAttribute(ALLOW_CREATE_ATTR_KEY, null);

                            if (!city.isRequireInvite()) {
                                Bukkit.broadcastMessage(ChatColor.GRAY + "L'Histoire de la ville de " + city.shortDisplayable() + ChatColor.GRAY + " commence ce jour... Que le sort soit avec elle !");
                            }
                            Messages.debit(player, amt, "création de ville");
                            city.creditMoney(amt * 0.9); // Creation amount minus 10% TAX
                        } else {
                            data.creditMoney(amt);
                            player.sendMessage(ChatColor.RED + "Une erreur s'est produite.");
                        }
                    }
                }
            }
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Syntaxe invalide : '/" + command + " help " + subCommand + "' pour de l'aide");
            return false;
        }
    }
}
