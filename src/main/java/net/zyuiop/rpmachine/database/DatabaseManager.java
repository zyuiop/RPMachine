package net.zyuiop.rpmachine.database;

import java.io.IOException;
import java.util.UUID;

/**
 * @author zyuiop
 */
public interface DatabaseManager {
	void load() throws IOException;

	PlayerData getPlayerData(UUID uuid);

	UUIDTranslator getUUIDTranslator();

	ShopsManager getShopsManager();
}
