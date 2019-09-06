package net.zyuiop.rpmachine.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.LegalEntityType;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.permissions.EconomyPermissions;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class CommandPay extends AbstractCommand {
    public CommandPay() {
        super("pay", null, "payer", "send");
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Utilisation : /pay [city|player|project|admin] [joueur à payer] <montant à payer>");
            return true;
        }

        RoleToken transactionFrom = RPMachine.getPlayerRoleToken(player);
        double val = Double.parseDouble(args[args.length - 1]);
        args = Arrays.copyOfRange(args, 0, args.length - 1);
        if (val < 0.01) {
            player.sendMessage(ChatColor.RED + "Impossible d'envoyer une somme négative ou nulle.");
        } else if (!transactionFrom.hasDelegatedPermission(EconomyPermissions.PAY_MONEY_TO_PLAYER)) {
            player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de faire ça en tant que " + transactionFrom.getLegalEntity().displayable());
        } else {
            try {
                LegalEntity target = LegalEntityType.getLegalEntity(player, "player", args);

                if (!transactionFrom.getLegalEntity().transfer(val, target)) {
                    Messages.notEnoughMoneyEntity(player, transactionFrom.getLegalEntity(), val);
                } else {
                    Messages.credit(target, val, "transfert de " + transactionFrom.getLegalEntity().displayable());
                    Messages.debitEntity(player, transactionFrom.getLegalEntity(), val, "transfert à " + target.displayable());
                }
            } catch (CommandException ex) {
                player.sendMessage(ChatColor.RED + ex.getMessage());
            }
        }
        return true;
    }
}
