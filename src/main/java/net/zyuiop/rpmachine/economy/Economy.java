package net.zyuiop.rpmachine.economy;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.FinancialCallback;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.entities.AccountHolder;
import net.zyuiop.rpmachine.entities.LegalEntity;

import java.util.UUID;
import java.util.function.Consumer;

public class Economy {
    private static String moneyName = null;
    private static double baseAmount = -1;

    public static String getCurrencyName() {
        if (moneyName == null) {
            moneyName = RPMachine.getInstance().getConfig().getString("money.symbol", "$");
        }
        return moneyName;
    }

    public static double getCreationBalance() {
        if (baseAmount == -1) {
            baseAmount = RPMachine.getInstance().getConfig().getDouble("money.baseAmount", 150D);
        }
        return baseAmount;
    }
}
