package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.citysubcommands.*;
import net.zyuiop.rpmachine.cities.commands.citysubcommands.params.CityParamsSubCommand;
import net.zyuiop.rpmachine.cities.commands.citysubcommands.params.SetTaxesCommand;
import net.zyuiop.rpmachine.cities.commands.citysubcommands.params.SetTypeCommand;
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
        registerSubCommand("remove", new RemoveCommand(citiesManager));
        registerSubCommand("setmayor", new SetMayorCommand(citiesManager));
        registerSubCommand("paytaxes", new PayTaxesCommand(citiesManager), "pay");
        registerSubCommand("unpaidtaxes", new UnpaidTaxesCommand());
        registerSubCommand("simulatetaxes", new SimulateTaxesCommand());
        registerSubCommand("kick", new KickCommand(citiesManager));
        registerSubCommand("create", new CreateCityCommand(citiesManager));
        registerSubCommand("permissions", new PermissionsCommand());
        registerSubCommand("params", new CityParamsSubCommand(citiesManager), "p", "prefs");
        registerSubCommand("fusion", new FusionCommand(citiesManager), "merge");

        if (RPMachine.isTpEnabled())
            registerSubCommand("teleport", new TeleportCommand(citiesManager), "tp");

        // Will also register a command with the same name
        FloorsCommand fc = new FloorsCommand();
        registerSubCommand("floors", fc, "paliers");
    }
}
