package net.zyuiop.rpmachine.database;

import java.util.UUID;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.economy.shops.AbstractShopSign;

/**
 * @author zyuiop
 */
public interface DatabaseManager {
	PlayerData getPlayerData(UUID uuid);

	UUIDTranslator getUUIDTranslator();

}
