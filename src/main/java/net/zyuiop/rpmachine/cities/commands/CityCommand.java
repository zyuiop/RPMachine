package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.citysubcommands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class CityCommand extends CitiesCommand {
	public CityCommand(CitiesManager citiesManager) {
		super(citiesManager);

		// Enregistrement des commandes
		registerSubCommand("info", new InfoCommand(citiesManager));
		registerSubCommand("join", new CityJoinCommand(citiesManager));
		registerSubCommand("leave", new CityLeaveCommand(citiesManager));
		registerSubCommand("claim", new ClaimCommand(citiesManager));
		registerSubCommand("list", new ListCommand(citiesManager));
		registerSubCommand("members", new MembersCommand(citiesManager));
		registerSubCommand("council", new CouncilCommand(citiesManager));
		registerSubCommand("givemoney", new GiveMoneyCommand(citiesManager));
		registerSubCommand("invite", new InviteCommand(citiesManager));
		registerSubCommand("setspawn", new SetSpawnCommand(citiesManager));
		registerSubCommand("settype", new SetTypeCommand(citiesManager));
		registerSubCommand("teleport", new TeleportCommand(citiesManager));
		registerSubCommand("remove", new RemoveCommand(citiesManager));
		registerSubCommand("setmayor", new SetMayorCommand(citiesManager));
		registerSubCommand("settaxes", new SetTaxesCommand(citiesManager));
		registerSubCommand("paytaxes", new PayTaxesCommand(citiesManager));
		registerSubCommand("unpaidtaxes", new UnpaidTaxesCommand(citiesManager));
		registerSubCommand("pay", new PayCommand(citiesManager));
		registerSubCommand("simulatetaxes", new SimulateTaxesCommand(citiesManager));

		aliases.put("i", "info");
		aliases.put("j", "join");
		aliases.put("c", "claim");
		aliases.put("gm", "givemoney");
		aliases.put("a", "invite");
		aliases.put("add", "invite");
		aliases.put("spawn", "setspawn");
		aliases.put("s", "setspawn");
		aliases.put("type", "settype");
		aliases.put("t", "settype");
		aliases.put("tp", "teleport");
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
		sender.sendMessage(ChatColor.GOLD + "-----[ "+ ChatColor.BOLD + "Commande de gestion de villes" + ChatColor.GOLD +" ]-----");
		for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
			sender.sendMessage(ChatColor.GREEN + "- /city " + entry.getKey() + " " + entry.getValue().getUsage() + " : " + ChatColor.YELLOW + entry.getValue().getDescription());
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
