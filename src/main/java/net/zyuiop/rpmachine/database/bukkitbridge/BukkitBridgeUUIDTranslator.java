package net.zyuiop.rpmachine.database.bukkitbridge;

import java.util.UUID;
import net.zyuiop.rpmachine.database.UUIDTranslator;

/**
 * @author zyuiop
 */
public class BukkitBridgeUUIDTranslator implements UUIDTranslator {
	private final net.bridgesapi.api.names.UUIDTranslator translator;

	public BukkitBridgeUUIDTranslator(net.bridgesapi.api.names.UUIDTranslator translator) {
		this.translator = translator;
	}

	@Override
	public UUID getUUID(String s, boolean b) {
		return translator.getUUID(s, b);
	}

	@Override
	public UUID getUUID(String name) {
		return translator.getUUID(name);
	}

	@Override
	public void cachePair(UUID uuid, String playerName) {
		// Drop
	}

	@Override
	public String getName(UUID uuid, boolean b) {
		return translator.getName(uuid, b);
	}

	@Override
	public String getName(UUID uuid) {
		return translator.getName(uuid);
	}
}
