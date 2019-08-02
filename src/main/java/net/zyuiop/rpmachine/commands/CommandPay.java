package net.zyuiop.rpmachine.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.permissions.EconomyPermissions;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CommandPay extends AbstractCommand {
    public CommandPay() {
        super("pay", null, "payer", "send");
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Utilisation : /pay <joueur à payer> <montant à payer>");
            return true;
        }

        // TODO: handle payment to other entities

        RoleToken transactionFrom = RPMachine.getPlayerRoleToken(player);
        Player target = Bukkit.getPlayerExact(args[0]);
        if (!transactionFrom.hasDelegatedPermission(EconomyPermissions.PAY_MONEY_TO_PLAYER)) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de faire ça en tant que " + transactionFrom.getLegalEntity().displayable());
        } else if (target == null) {
            player.sendMessage(ChatColor.RED + "Le joueur est actuellement hors ligne.");
            return true;
        } else if (target.getUniqueId().equals(player.getUniqueId()) && transactionFrom.getLegalEntity() instanceof PlayerData) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas vous donner d'argent à vous même.");
            return true;
        } else {
            double val = Double.parseDouble(args[1]);
            if (val < 0) {
                player.sendMessage(ChatColor.RED + "Impossible d'envoyer une somme négative.");
                return true;
            }

            PlayerData targetData = RPMachine.database().getPlayerData(target);

            if (!transactionFrom.getLegalEntity().transfer(val, targetData)) {
                Messages.notEnoughMoneyEntity(player, transactionFrom.getLegalEntity(), val);
            } else {
                Messages.credit(target, val, "transfert de " + transactionFrom.getLegalEntity().shortDisplayable());
                Messages.debitEntity(player, transactionFrom.getLegalEntity(), val, "transfert à " + target.getName());
            }
        }

        return true;
    }
}
