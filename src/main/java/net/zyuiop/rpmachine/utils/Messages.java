package net.zyuiop.rpmachine.utils;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.entities.LegalEntity;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public class Messages {
    private static String entityPrefix(Player player, LegalEntity entity) {
        if (entity == null || (entity instanceof PlayerData && ((PlayerData) entity).getUuid().equals(player.getUniqueId())))
            return "";
        return ChatColor.AQUA + "[" + entity.shortDisplayable() + ChatColor.AQUA + "] ";
    }

    public static void credit(Player player, double amount, String reason) {
        creditEntity(player, null, amount, reason);
    }

    public static void credit(LegalEntity entity, double amount, String reason) {
        entity.getOnlineAdministrators().forEach(pl -> creditEntity(pl, entity, amount, reason));


        entity.getOfflineAdministrators().forEach(adm -> RPMachine.getInstance().getDiscordManager()
                .sendMessage(adm, "Crédit " + amount + RPMachine.getCurrencyName() + ": " + reason));
    }

    public static void sendMessage(LegalEntity entity, String message) {
        entity.getOnlineAdministrators().forEach(pl -> pl.sendMessage(entityPrefix(pl, entity) + message));

        entity.getOfflineAdministrators().forEach(adm -> RPMachine.getInstance().getDiscordManager()
                .sendMessage(adm, "Notification: " + entity.displayable() + message));
    }

    public static void debit(LegalEntity entity, double amount, String reason) {
        entity.getOnlineAdministrators().forEach(pl -> debitEntity(pl, entity, amount, reason));

        entity.getOfflineAdministrators().forEach(adm -> RPMachine.getInstance().getDiscordManager()
                .sendMessage(adm, "Débit " + amount + RPMachine.getCurrencyName() + ": " + reason));
    }

    public static void debit(Player player, double amount, String reason) {
        debitEntity(player, null, amount, reason);
    }

    private static String formatReason(String reason) {
        return reason == null ? "" : (ChatColor.GRAY + " (" + reason + ChatColor.GRAY + ")");
    }

    public static void creditEntity(Player player, LegalEntity entity, double amount, String reason) {
        player.sendMessage(entityPrefix(player, entity) + ChatColor.GREEN + " + " + amount + " " + RPMachine.getCurrencyName() + formatReason(reason));
    }

    public static void debitEntity(Player player, LegalEntity entity, double amount, String reason) {
        player.sendMessage(entityPrefix(player, entity) + ChatColor.RED + " - " + amount + " " + RPMachine.getCurrencyName() + formatReason(reason));
    }

    public static void notEnoughMoney(Player player, double amount) {
        notEnoughMoneyEntity(player, null, amount);
    }

    public static void notEnoughMoneyEntity(Player player, LegalEntity entity, double amount) {
        player.sendMessage(entityPrefix(player, entity) + ChatColor.RED + "Fonds insuffisants. Montant nécessaire : " + ChatColor.DARK_RED + amount + " " + RPMachine.getCurrencyName());
    }
}
