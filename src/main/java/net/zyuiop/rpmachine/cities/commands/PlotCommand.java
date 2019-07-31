package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.plotsubcommands.*;
import net.zyuiop.rpmachine.common.commands.CompoundCommand;


public class PlotCommand extends CompoundCommand {
    public PlotCommand(CitiesManager citiesManager) {
        super("plot", null, "plots", "p", "parcelles", "parcelle");

        // Enregistrement des commandes
        registerSubCommand("create", new CreateCommand(citiesManager), "c");
        registerSubCommand("list", new ListCommand(citiesManager), "l");
        registerSubCommand("members", new MembersCommand(citiesManager), "m");
        registerSubCommand("remove", new RemoveCommand(citiesManager));
        registerSubCommand("leave", new LeaveCommand(citiesManager));
        registerSubCommand("wand", new WandCommand());
        registerSubCommand("redefine", new RedefineCommand(citiesManager));
        registerSubCommand("info", new InfoCommand(citiesManager), "i");
    }
}
