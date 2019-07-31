package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.citysubcommands.*;
import net.zyuiop.rpmachine.commands.CompoundCommand;

public class CityCommand extends CompoundCommand {
    public CityCommand(CitiesManager citiesManager) {
        super("city", null, "cities", "c", "ville", "v");
        // Enregistrement des commandes
        registerSubCommand("info", new InfoCommand(citiesManager), "i");
        registerSubCommand("join", new CityJoinCommand(citiesManager), "j", "rejoindre");
        registerSubCommand("leave", new CityLeaveCommand(), "quitter");
        registerSubCommand("claim", new ClaimCommand(citiesManager), "c");
        registerSubCommand("list", new ListCommand(), "liste", "l");
        registerSubCommand("members", new MembersCommand(), "membres", "m");
        registerSubCommand("council", new CouncilCommand(citiesManager), "conseil", "adjoints");
        registerSubCommand("givemoney", new GiveMoneyCommand(citiesManager), "gm", "gift", "give");
        registerSubCommand("invite", new InviteCommand(citiesManager), "add");
        registerSubCommand("setspawn", new SetSpawnCommand(citiesManager));
        registerSubCommand("settype", new SetTypeCommand(citiesManager));
        registerSubCommand("teleport", new TeleportCommand(citiesManager), "tp");
        registerSubCommand("remove", new RemoveCommand(citiesManager));
        registerSubCommand("setmayor", new SetMayorCommand(citiesManager));
        registerSubCommand("settaxes", new SetTaxesCommand(citiesManager));
        registerSubCommand("paytaxes", new PayTaxesCommand(citiesManager), "pay");
        registerSubCommand("unpaidtaxes", new UnpaidTaxesCommand());
        registerSubCommand("simulatetaxes", new SimulateTaxesCommand());
        registerSubCommand("kick", new KickCommand(citiesManager));
    }
}
