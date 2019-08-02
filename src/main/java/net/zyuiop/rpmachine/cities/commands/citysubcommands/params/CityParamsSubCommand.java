package net.zyuiop.rpmachine.cities.commands.citysubcommands.params;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.commands.CompoundSubCommand;

/**
 * @author Louis Vialar
 */
public class CityParamsSubCommand extends CompoundSubCommand {
    public CityParamsSubCommand(CitiesManager citiesManager) {
        super("", "règle les préférences de la ville");

        registerSubCommand("spawn", new SetSpawnCommand(citiesManager), "setspawn");
        registerSubCommand("type", new SetTypeCommand(citiesManager), "settype");
        registerSubCommand("taxes", new SetTaxesCommand(citiesManager), "settaxes");
        registerSubCommand("selltaxes", new SetSellTaxCommand(citiesManager), "selltax");
        registerSubCommand("tptax", new SetTpTaxCommand(citiesManager), "tptaxes");
        registerSubCommand("jointax", new SetJoinTaxCommand(citiesManager), "jointaxes");
    }
}
