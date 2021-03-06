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

public class PayTaxesCommand implements SubCommand {
    private final CitiesManager citiesManager;

    public PayTaxesCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<ville>";
    }

    @Override
    public String getDescription() {
        return "paye vos impôts en retard";
    }

    @Override
    public boolean canUse(Player player) {
        return RPMachine.getPlayerRoleToken(player).hasDelegatedPermission(EconomyPermissions.PAY_LATE_TAXES);
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Utilisation incorrecte : utilisez /city paytaxes " + getUsage());
            return false;
        } else {
            City city = citiesManager.getCity(args[0]);
            if (city == null) {
                player.sendMessage(ChatColor.RED + "Cette ville n'exite pas.");
                return false;
            }

            LegalEntity payer = RPMachine.getPlayerRoleToken(player).getLegalEntity();
            double topay = payer.getUnpaidTaxes(city.getCityName());
            if (topay == 0D) {
                player.sendMessage(ChatColor.GREEN + "Vous ne devez pas d'argent à cette ville.");
            } else {
                double amount = payer.getBalance();
                if (amount >= topay) {
                    RPMachine.database().getPlayerData(player).transfer(topay, city);
                    Messages.debit(player, topay, "paiement des impôts");
                    player.sendMessage(ChatColor.GREEN + "Vous ne devez plus rien à cette ville.");

                    payer.setUnpaidTaxes(city.getCityName(), 0D);
                    city.payTaxes(payer, topay);
                } else {
                    RPMachine.database().getPlayerData(player).transfer(amount, city);
                    Messages.debit(player, topay, "paiement des impôts");
                    player.sendMessage(ChatColor.RED + "Vous devez encore " + topay + " " + RPMachine.getCurrencyName() + " à la ville.");

                    topay = topay - amount;
                    payer.setUnpaidTaxes(city.getCityName(), topay);
                    city.payTaxes(payer, amount);
                }
                citiesManager.saveCity(city);
            }
            return true;
        }
    }
}
