package net.zyuiop.rpmachine.commands;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.zyuiop.rpmachine.RPMachine;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Louis Vialar
 */
public abstract class AbstractCommand implements TabCompleter, CommandExecutor {
    private final String requiredPerms;

    protected AbstractCommand(String command, String permission, String... aliases) {
        this.requiredPerms = permission;
        
        try {
            JavaPlugin plugin = RPMachine.getInstance();
            Class<PluginCommand> clazz = PluginCommand.class;
            Constructor<PluginCommand> ctor = clazz.getDeclaredConstructor(String.class, Plugin.class);
            ctor.setAccessible(true);
            PluginCommand com = ctor.newInstance(command, plugin);
            com.setAliases(Lists.newArrayList(aliases));
            com.setExecutor(this);
            com.setTabCompleter(this);
            ((CraftServer) Bukkit.getServer()).getCommandMap().register(plugin.getName(), com);

            Bukkit.getLogger().info("Registered command " + command);
        } catch (ReflectiveOperationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed registering command " + command, ex);
        }
    }

    protected abstract boolean onPlayerCommand(Player player, String command, String[] args);

    protected boolean onNonPlayerCommand(CommandSender console, String command, String[] args) {
        console.sendMessage(ChatColor.RED + "Only players can run this command.");
        return false;
    }

    @Override
    public final boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            return onNonPlayerCommand(commandSender, s, strings);
        } else {
            Player player = (Player) commandSender;

            if (!hasPermission(player)) {
                player.sendMessage(ChatColor.RED + "You don't have the permission to run this command.");
                return false;
            }

            return onPlayerCommand(player, s, strings);
        }
    }

    protected boolean hasPermission(Player player) {
        return requiredPerms == null || player.hasPermission(requiredPerms);
    }

    @Override
    public final List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            return onNonPlayerTabComplete(commandSender, s, args);
        }
        Player player = (Player) commandSender;
        if (hasPermission(player)) {
            return onPlayerTabComplete(player, s, args);
        }
        return null;
    }

    /**
     * Overide to set the tabComplete result
     *
     * @param player The player who sent the command
     * @param command The command sent
     * @param args The already defined arguments
     * @return A list of possible completions for the last given argument
     */
    protected List<String> onPlayerTabComplete(Player player, String command, String... args) {
        return null;
    }

    /**
     * Overide to set the tabComplete result
     *
     * @param player The player who sent the command
     * @param command The command sent
     * @param args The already defined arguments
     * @return A list of possible completions for the last given argument
     */
    protected List<String> onNonPlayerTabComplete(CommandSender player, String command, String... args) {
        return null;
    }
}
