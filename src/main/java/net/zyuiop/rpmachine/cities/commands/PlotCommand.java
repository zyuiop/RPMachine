package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.plotsubcommands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class PlotCommand extends CitiesCommand {
	public PlotCommand(CitiesManager citiesManager) {
		super(citiesManager);

		// Enregistrement des commandes
		registerSubCommand("create", new CreateCommand(citiesManager));
		registerSubCommand("list", new ListCommand(citiesManager));
		registerSubCommand("members", new MembersCommand(citiesManager));
		registerSubCommand("remove", new RemoveCommand(citiesManager));
		registerSubCommand("leave", new LeaveCommand(citiesManager));
		registerSubCommand("wand", new WandCommand(citiesManager));
		registerSubCommand("redefine", new RedefineCommand(citiesManager));
		registerSubCommand("info", new InfoCommand(citiesManager));

		aliases.put("c", "create");
		aliases.put("l", "list");
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
		sender.sendMessage(ChatColor.GOLD + "-----[ "+ ChatColor.BOLD + "Commande de gestion de parcelles" + ChatColor.GOLD +" ]-----");
		for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
			sender.sendMessage(ChatColor.GREEN + "- /plot " + entry.getKey() + " " + entry.getValue().getUsage() + " : " + ChatColor.YELLOW + entry.getValue().getDescription());
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
