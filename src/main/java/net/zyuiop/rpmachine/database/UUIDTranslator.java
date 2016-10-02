package net.zyuiop.rpmachine.database;

import java.util.UUID;

/**
 * @author zyuiop
 */
public interface UUIDTranslator {

	String getName(UUID uuid, boolean allowMojangCheck);

	default String getName(UUID uuid) {
		return getName(uuid, true);
	}

	UUID getUUID(String name, boolean allowMojangCheck);

	default UUID getUUID(String name) {
		return getUUID(name, true);
	}

}
