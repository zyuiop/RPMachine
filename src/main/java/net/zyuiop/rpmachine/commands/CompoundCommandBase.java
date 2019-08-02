package net.zyuiop.rpmachine.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
interface CompoundCommandBase {

    Map<String, SubCommand> subCommands();

    default List<String> genTabComplete(Player player, String... args) {
        if (args.length > 0 && subCommands().containsKey(args[0].toLowerCase())) {
            SubCommand sc = subCommands().get(args[0].toLowerCase());
            if (sc.canUse(player))
                return sc.tabComplete(player, Arrays.copyOfRange(args, 1, args.length));
            return null;
        }

        return subCommands().entrySet().stream()
                .filter(entry -> entry.getValue().canUse(player)).map(Map.Entry::getKey)
                .filter(key -> args.length == 0 || key.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }

    default void registerHelp() {
        registerSubCommand("help", new SubCommand() {
            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public boolean hasHelp() {
                return false;
            }

            @Override
            public String getUsage() {
                return null;
            }

            @Override
            public boolean canUse(Player player) {
                return true;
            }

            @Override
            public boolean run(Player player, String command, String subCommand, String... args) {
                if (args.length > 0) {
                    SubCommand sc = subCommands().get(args[0].toLowerCase());
                    if (sc == null) {
                        player.sendMessage(ChatColor.RED + "Commande introuvable. Essayez " + ChatColor.DARK_RED + "/" + command + " help" + ChatColor.RED + " pour plus d'informations.");
                    } else if (!sc.canUse(player)) {
                        player.sendMessage(ChatColor.RED + "Vous ne pouvez pas exécuter cette sous commande (permission ou conditions manquantes). Essayez " + ChatColor.DARK_RED + "/" + command + " help" + ChatColor.RED + " pour plus d'informations.");
                    } else {
                        StringBuilder help = new StringBuilder("§e/").append(command);
                        String usage = sc.getUsage();
                        String helpText = sc.getDescription();

                        if (usage != null) {
                            help.append(" ").append(subCommand).append(" ").append(usage);
                        }

                        if (helpText != null) {
                            help.append(": §r").append(helpText);
                        }

                        player.sendMessage(help.toString());
                    }
                }
                printHelp(player, command);
                return true;
            }
        });
    }

    default boolean runCommand(Player player, String command, String[] args) {
        if (args.length == 0) {
            printHelp(player, command);
            return true;
        }

        SubCommand subCommand = subCommands().get(args[0].toLowerCase());
        if (subCommand == null) {
            player.sendMessage(ChatColor.RED + "Commande introuvable. Essayez " + ChatColor.DARK_RED + "/" + command + " help" + ChatColor.RED + " pour plus d'informations.");
            return false;
        }

        if (!subCommand.canUse(player)) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas exécuter cette sous commande (permission ou conditions manquantes). Essayez " + ChatColor.DARK_RED + "/" + command + " help" + ChatColor.RED + " pour plus d'informations.");
            return false;
        }

        String[] newArgs;
        if (args.length > 1)
            newArgs = Arrays.copyOfRange(args, 1, args.length);
        else
            newArgs = new String[0];
        return subCommand.run(player, command, args[0], newArgs);
    }

    default void printHelp(Player player, String commandBase) {
        subCommands().entrySet().stream()
                .filter(p -> p.getValue().hasHelp())
                .filter(subCommand -> subCommand.getValue().canUse(player))
                .forEach(subCommand -> {
                    StringBuilder help = new StringBuilder("§e/").append(commandBase);
                    String usage = subCommand.getValue().getUsage();
                    String helpText = subCommand.getValue().getDescription();

                    if (usage != null) {
                        help.append(" ").append(subCommand.getKey()).append(" ").append(usage);
                    }

                    if (helpText != null) {
                        help.append(": §r").append(helpText);
                    }

                    player.sendMessage(help.toString());
                });
    }

    default void registerSubCommand(String name, SubCommand subCommand, String... aliases) {
        subCommands().put(name, subCommand);
        for (String alias : aliases) {
            subCommands().put(alias, new AliasSubCommand(subCommand));
        }
    }

    static class AliasSubCommand implements SubCommand {
        private final SubCommand subCommand;

        AliasSubCommand(SubCommand command) {
            subCommand = command;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String getUsage() {
            return null;
        }

        @Override
        public boolean canUse(Player player) {
            return subCommand.canUse(player);
        }

        @Override
        public boolean run(Player player, String command, String subCommand, String... args) {
            return this.subCommand.run(player, command, subCommand, args);
        }

        @Override
        public boolean hasHelp() {
            return false;
        }
    }
}
