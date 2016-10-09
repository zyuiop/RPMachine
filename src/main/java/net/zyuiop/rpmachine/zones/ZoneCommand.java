package net.zyuiop.rpmachine.zones;

import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.zones.subcommands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ZoneCommand implements CommandExecutor {
	public ZoneCommand(ZonesManager manager) {

		// Enregistrement des commandes
		registerSubCommand("create", new CreateCommand(manager));
		registerSubCommand("members", new MembersCommand(manager));
		registerSubCommand("remove", new RemoveCommand(manager));
		registerSubCommand("leave", new LeaveCommand(manager));
		registerSubCommand("wand", new WandCommand());
		registerSubCommand("redefine", new RedefineCommand(manager));
		registerSubCommand("info", new InfoCommand(manager));

		aliases.put("c", "create");
		aliases.put("i", "info");
		aliases.put("m", "members");
		aliases.put("r", "remove");
		aliases.put("w", "wand");
	}

	private HashMap<String, SubCommand> subCommands = new HashMap<>();
	private HashMap<String, String> aliases = new HashMap<>();

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		if (strings.length == 0) {
			showHelp(commandSender);
		} else {
			String operation = strings[0];
			SubCommand sub = get(operation);
			if (sub == null) {
				showHelp(commandSender);
			} else {
				new Thread(() -> sub.run(commandSender, Arrays.copyOfRange(strings, 1, strings.length))).run();
			}
		}
		return true;
	}

	private void showHelp(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "-----[ " + ChatColor.BOLD + "Commande de gestion de zones" + ChatColor.GOLD + " ]-----");
		for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
			sender.sendMessage(ChatColor.GREEN + "- /zone " + entry.getKey() + " " + entry.getValue().getUsage() + " : " + ChatColor.YELLOW + entry.getValue().getDescription());
		}
	}

	public void registerSubCommand(String commandName, SubCommand command) {
		subCommands.put(commandName, command);
	}

	private SubCommand get(String command) {
		for (String com : subCommands.keySet()) {
			if (com.equalsIgnoreCase(command))
				return subCommands.get(com);
		}

		for (String alias : aliases.keySet()) {
			if (alias.equalsIgnoreCase(command))
				return subCommands.get(aliases.get(alias));
		}
		return null;
	}
}
