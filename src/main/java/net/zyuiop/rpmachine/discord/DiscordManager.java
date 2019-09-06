package net.zyuiop.rpmachine.discord;

import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Channel;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.City;
import org.bukkit.Bukkit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
public class DiscordManager {
    private DiscordClient api;

    public DiscordManager(String token) {
        api = token != null ? new DiscordClientBuilder(token)
                .build() : null;

        if (api == null) {
            Bukkit.getLogger().warning("No discord API - discarding");
            return;
        }

        registerCommand(new DiscordLinkingManager());

        api.login().subscribe();
        Bukkit.getLogger().info("Discord bot started.");

        Bukkit.getScheduler().runTaskTimer(RPMachine.getInstance(), (Runnable) this::refreshPermissions, 100L, 5 * 20 * 60L);
    }

    public void registerCommand(DiscordCommand command) {
        api.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(msg -> msg.getContent().map(str -> str.startsWith(command.command)).orElse(false))
                .filter(msg -> !command.usersOnly || msg.getAuthor().isPresent())
                .flatMap(command::runCommand)
                .subscribe();
    }

    public void createChannels(City city) {
        api.getGuilds().flatMap(guild -> {
            //Mono<Role> everyoneMono = guild.getEveryoneRole();

            Flux<GuildChannel> categories = guild.getChannels()
                    .filter(p -> p.getType() == Channel.Type.GUILD_CATEGORY)
                    .filter(p -> p.getName().toLowerCase().contains("villes"));

            Mono<GuildChannel> privateCategory = categories.filter(c -> c.getName().toLowerCase().contains("privé")).last();
            Mono<GuildChannel> publicCategory = categories.filter(c -> c.getName().toLowerCase().contains("public")).last();

            Set<PermissionOverwrite> admins = getAdmins(city);

            Mono<Channel> privateChan = privateCategory.flatMap(cat -> /*everyoneMono.flatMap(everyone -> */{
                        Set<PermissionOverwrite> members = getMembers(city);

                        members.addAll(admins);
                        members.add(PermissionOverwrite.forRole(guild.getId(), PermissionSet.none(), PermissionSet.of(Permission.READ_MESSAGE_HISTORY, Permission.SEND_MESSAGES)));

                        return guild.createTextChannel(c -> c.setName(city.getCityName() + "-privé")
                                .setTopic("Discussions privées pour la ville de " + city.getCityName())
                                .setParentId(cat.getId())
                                .setPermissionOverwrites(members));
                    }
            );

            Mono<Channel> publicChan = publicCategory.flatMap(cat ->
                    guild.createTextChannel(c -> c.setName(city.getCityName() + "-public")
                            .setTopic("Discussions publiques relatives à la ville de " + city.getCityName())
                            .setParentId(cat.getId())
                            .setPermissionOverwrites(admins))
            );

            return privateChan.doOnSuccess(chan -> city.setPrivateChannelId(chan.getId().asLong()))
                    .and(publicChan.doOnSuccess(chan -> city.setPublicChannelId(chan.getId().asLong())));

        }).subscribe(v -> city.save());
    }

    public void sendMessage(long userId, String message) {
        api.getUserById(Snowflake.of(userId)).flatMap(User::getPrivateChannel)
                .flatMap(pv -> pv.createMessage(message))
                .subscribe();
    }

    private Set<PermissionOverwrite> getAdmins(City city) {
        return city.getCouncils().stream().map(id -> RPMachine.getInstance().getDatabaseManager().getPlayerData(id))
                .filter(data -> data.hasAttribute(DiscordLinkingManager.DISCORD_USER_ID))
                .map(user -> Snowflake.of((long) user.getAttribute(DiscordLinkingManager.DISCORD_USER_ID)))
                .map(flake -> PermissionOverwrite.forMember(flake, PermissionSet.of(Permission.READ_MESSAGE_HISTORY, Permission.SEND_MESSAGES, Permission.MANAGE_MESSAGES, Permission.VIEW_CHANNEL), PermissionSet.none()))
                .collect(Collectors.toSet());
    }


    private Set<PermissionOverwrite> getMembers(City city) {
        return city.getInhabitants().stream().map(id -> RPMachine.getInstance().getDatabaseManager().getPlayerData(id))
                .filter(data -> data.hasAttribute(DiscordLinkingManager.DISCORD_USER_ID))
                .map(user -> Snowflake.of((long) user.getAttribute(DiscordLinkingManager.DISCORD_USER_ID)))
                .map(flake -> PermissionOverwrite.forMember(flake, PermissionSet.of(Permission.READ_MESSAGE_HISTORY, Permission.SEND_MESSAGES, Permission.VIEW_CHANNEL), PermissionSet.none()))
                .collect(Collectors.toCollection(HashSet::new));
    }

    public Mono<Void> refreshPermissions(City city) {
        if (city.getPrivateChannelId() == 0 || city.getPublicChannelId() == 0)
            return Mono.empty();

        return api.getGuilds().flatMap(guild -> {
            //Mono<Role> everyoneMono = guild.getEveryoneRole();

            Set<PermissionOverwrite> admins = getAdmins(city);

            Mono<Channel> priv = guild.getChannelById(Snowflake.of(city.getPrivateChannelId())).flatMap(chan -> {
                Set<PermissionOverwrite> members = getMembers(city);
                members.addAll(admins);
                members.add(PermissionOverwrite.forRole(guild.getId(), PermissionSet.none(), PermissionSet.of(Permission.READ_MESSAGE_HISTORY, Permission.SEND_MESSAGES, Permission.VIEW_CHANNEL)));

                return ((TextChannel) chan).edit(tc -> tc.setPermissionOverwrites(members));
            });

            Mono<Channel> pub = guild.getChannelById(Snowflake.of(city.getPublicChannelId())).flatMap(chan -> ((TextChannel) chan).edit(tc -> tc.setPermissionOverwrites(admins)));

            return priv.and(pub);

        }).reduce((a, b) -> a);
    }

    public void refreshPermissions() {
        Bukkit.getLogger().info("Refreshing discord permissions...");

        RPMachine.getInstance().getCitiesManager().getCities().stream()
                .map(this::refreshPermissions)
                .reduce(Mono.empty(), Mono::and)
                .subscribe(r -> Bukkit.getLogger().info("Done refreshing discord permissions for all cities."));
    }
}
