package net.zyuiop.rpmachine.database;

import java.util.UUID;

/**
 * @author zyuiop
 */
public interface UUIDTranslator {

	String getName(UUID uuid);

	UUID getUUID(String name);

	void cachePair(UUID uuid, String playerName);
}
