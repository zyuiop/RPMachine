package net.zyuiop.rpmachine.cities.commands.citysubcommands.params;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.commands.CompoundSubCommand;

/**
 * @author Louis Vialar
 */
public class CityParamsSubCommand extends CompoundSubCommand {
    public CityParamsSubCommand(CitiesManager citiesManager) {
        super("", "règle les préférences de la ville");

        registerSubCommand("type", new SetTypeCommand(citiesManager), "settype");
        registerSubCommand("taxes", new SetTaxesCommand(citiesManager), "settaxes");
        registerSubCommand("vat", new SetVATCommand(citiesManager), "tva");
        registerSubCommand("selltaxes", new SetSellTaxCommand(citiesManager), "selltax");
        if (RPMachine.isTpEnabled()) {
            registerSubCommand("tptax", new SetTpTaxCommand(citiesManager), "tptaxes");
            registerSubCommand("spawn", new SetSpawnCommand(citiesManager), "setspawn");
        }
        registerSubCommand("jointax", new SetJoinTaxCommand(citiesManager), "jointaxes");
        registerSubCommand("color", new SetChatColorCommand(citiesManager));
    }
}
