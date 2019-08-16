package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.permissions.EconomyPermissions;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GiveMoneyCommand implements SubCommand {

    private final CitiesManager citiesManager;

    public GiveMoneyCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<montant à donner> [nom de la ville]";
    }

    @Override
    public String getDescription() {
        return "effectue un don de monnaie à une ville";
    }

    @Override
    public boolean canUse(Player player) {
        return RPMachine.getPlayerRoleToken(player).hasDelegatedPermission(EconomyPermissions.PAY_MONEY_TO_CITY);
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {
        City c = citiesManager.getPlayerCity(player.getUniqueId());
        if (c == null && args.length < 2) {
            player.sendMessage(ChatColor.RED + "Vous n'êtes membre d'aucune ville.");
            return false;
        } else if (args.length > 1) {
            c = citiesManager.getCity(args[1]);
        }

        final City city = c;

        if (c == null) {
            player.sendMessage(ChatColor.RED + "Cette ville n'existe pas.");
            return false;
        } else {
            if (args.length < 1) {
                player.sendMessage(ChatColor.RED + "Utilisation incorrecte : /city givemoney " + getUsage());
                return false;
            } else {
                String amtStr = args[0];
                try {
                    double amt = Double.valueOf(amtStr);
                    if (amt <= 0) {
                        player.sendMessage(ChatColor.RED + "Montant trop faible.");
                    }

                    LegalEntity le = RPMachine.getPlayerActAs(player);

                    if (!le.transfer(amt, city)) {
                        Messages.notEnoughMoneyEntity(player, le, amt);
                    } else {
                        Messages.debitEntity(player, le, amt, "transfert à " + city.shortDisplayable());
						Messages.credit(city, amt, "transfert de " + le.displayable());
                    }
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Le montant fourni est invalide.");
                    return false;
                }
                citiesManager.saveCity(city);
            }
        }
        return true;
    }

}
