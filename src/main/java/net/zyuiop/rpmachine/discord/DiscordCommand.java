package net.zyuiop.rpmachine.discord;

import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

/**
 * @author Louis Vialar
 */
public abstract class DiscordCommand {
    final String command;
    final boolean usersOnly;

    protected DiscordCommand(String command, boolean usersOnly) {
        this.command = command;
        this.usersOnly = usersOnly;
    }

    protected abstract <R> Mono<R> runCommand(Message message);
}
