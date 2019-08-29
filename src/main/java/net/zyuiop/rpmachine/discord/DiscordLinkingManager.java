package net.zyuiop.rpmachine.discord;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.database.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Louis Vialar
 */
public class DiscordLinkingManager extends DiscordCommand {
    public static final String DISCORD_USERNAME_TAG = "discord_account_user";
    public static final String DISCORD_DISCRIMINATOR_TAG = "discord_account_discr";
    public static final String DISCORD_USER_ID = "discord_account_id";

    private static final SecureRandom random = new SecureRandom();
    private static final String allowedChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private Map<UUID, DiscordLinkRequest> requests = new ConcurrentHashMap<>();

    public DiscordLinkingManager() {
        super("!link", true);

        // Create new craft command
        new AbstractCommand("discord", null) {
            @Override
            protected boolean onPlayerCommand(Player player, String command, String[] args) {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Utilisation : /discord link <code>");
                } else {
                    String code = args[1];
                    finishLink(player, code);
                }
                return true;
            }
        };
    }

    private static String generateCode() {
        int max = allowedChars.length();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 8; ++i) {
            sb.append(allowedChars.charAt(random.nextInt(max)));

            if (i == 3)
                sb.append("-");
        }

        return sb.toString();
    }

    public void finishLink(Player player, String code) {
        UUID uuid = player.getUniqueId();
        DiscordLinkRequest linkRequest = requests.get(uuid);

        if (linkRequest == null) {
            player.sendMessage(ChatColor.RED + "Aucune requête ouverte pour votre compte. Depuis discord, lancez " + ChatColor.YELLOW + "!link " + player.getName());
            return;
        }

        if (System.currentTimeMillis() > linkRequest.exp) {
            requests.remove(uuid);
            player.sendMessage(ChatColor.RED + "La requête a expiré, merci de recommencer depuis Discord.");
            return;
        }

        code = code.toUpperCase().trim();
        if (!linkRequest.code.equals(code)) {
            player.sendMessage(ChatColor.RED + "Le code est incorrect.");

            if (++linkRequest.fails == 3) {
                requests.remove(uuid);
                player.sendMessage(ChatColor.RED + "Trop de tentatives échouées, merci de recommencer depuis Discord.");
            }

            return;
        }

        requests.remove(uuid);
        PlayerData data = RPMachine.getInstance().getDatabaseManager().getPlayerData(player);
        data.setAttribute(DISCORD_USERNAME_TAG, linkRequest.discordUser.getUsername());
        data.setAttribute(DISCORD_DISCRIMINATOR_TAG, linkRequest.discordUser.getDiscriminator());
        data.setAttribute(DISCORD_USER_ID, linkRequest.discordUser.getId().asLong());


        player.sendMessage(ChatColor.GREEN + "Compte discord lié.");
        linkRequest.discordUser.getPrivateChannel().flatMap(chan -> chan.createMessage("Votre compte discord a bien été lié au compte minecraft **" + player.getName() + "**.")).subscribe();
    }

    @Override
    protected Mono<Message> runCommand(Message message) {
        User author = message.getAuthor().get();
        String content = message.getContent().get();
        String[] parts = content.split(" ");
        if (parts.length < 2)
            return message.getChannel().flatMap(chan -> chan.createMessage("Oups : n'oubliez pas d'indiquer votre pseudo minecraft ! _!link Ceyal_ par exemple."));

        String username = parts[1];
        Player player = Bukkit.getPlayerExact(username);

        if (player == null || !player.isOnline()) {
            return message.getChannel().flatMap(chan -> chan.createMessage("Oups : vous devez être connecté en jeu pour pouvoir utiliser cette commande !"));
        }

        DiscordLinkRequest req = new DiscordLinkRequest();
        req.mcPlayer = player;
        req.discordUser = author;
        req.exp = System.currentTimeMillis() + 5 * 60 * 1000L;
        req.code = generateCode();

        requests.put(player.getUniqueId(), req);

        return author.getPrivateChannel().flatMap(chan ->
                chan.createMessage("Vous avez décidé de lier votre compte Minecraft avec ce compte discord.\n\n" +
                        "Dirigez vous en jeu et lancez la commande **/discord link " + req.code + "** pour valider."));

    }

    private static class DiscordLinkRequest {
        private int fails;
        private long exp;
        private String code;
        private User discordUser;
        private Player mcPlayer;
    }
}
