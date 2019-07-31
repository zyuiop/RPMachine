package net.zyuiop.rpmachine.database;

import net.zyuiop.rpmachine.entities.LegalEntityRepository;

import java.io.IOException;
import java.util.UUID;

/**
 * @author zyuiop
 */
public interface DatabaseManager extends LegalEntityRepository<PlayerData> {
	void load() throws IOException;

	PlayerData getPlayerData(UUID uuid);

	UUIDTranslator getUUIDTranslator();

	ShopsManager getShopsManager();
}
