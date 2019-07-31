package net.zyuiop.rpmachine.common.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
public class CompoundCommand extends AbstractCommand {
    private final String commandName;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    {
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
            public boolean run(Player player, String... args) {
                printHelp(player);
                return true;
            }
        });
    }

    public CompoundCommand(String command, String requiredPermission, String... aliases) {
        super(command, requiredPermission, aliases);
        this.commandName = command;
    }

    @Override
    protected List<String> onPlayerTabComplete(Player player, String command, String... args) {
        if (args.length > 0 && subCommands.containsKey(args[0].toLowerCase())) {
            SubCommand sc = subCommands.get(args[0].toLowerCase());
            if (sc.canUse(player))
                return sc.tabComplete(player, Arrays.copyOfRange(args, 1, args.length));
            return null;
        }

        return subCommands.entrySet().stream()
                .filter(entry -> entry.getValue().canUse(player)).map(Map.Entry::getKey)
                .filter(key -> args.length == 0 || key.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean onPlayerCommand(Player player, String command, String[] args) {
        if (args.length == 0) {
            printHelp(player);
            return true;
        }

        SubCommand subCommand = subCommands.get(args[0].toLowerCase());
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
        return subCommand.run(player, newArgs);
    }

    protected void printHelp(Player player) {
        subCommands.entrySet().stream()
                .filter(p -> p.getValue().hasHelp())
                .filter(subCommand -> subCommand.getValue().canUse(player))
                .forEach(subCommand -> {
                    StringBuilder help = new StringBuilder("§e/").append(commandName);
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

    public void registerSubCommand(String name, SubCommand subCommand, String... aliases) {
        subCommands.put(name, subCommand);
        for (String alias : aliases) {
            subCommands.put(alias, new AliasSubCommand(subCommand));
        }
    }

    private static class AliasSubCommand implements SubCommand {
        private final SubCommand subCommand;

        private AliasSubCommand(SubCommand command) {
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
        public boolean run(Player player, String... args) {
            return subCommand.run(player, args);
        }

        @Override
        public boolean hasHelp() {
            return false;
        }
    }
}
