package net.zyuiop.rpmachine.database;

import java.util.UUID;

/**
 * @author zyuiop
 */
public interface DatabaseManager {
	PlayerData getPlayerData(UUID uuid);

	UUIDTranslator getUUIDTranslator();

	ShopsManager getShopsManager();
}
