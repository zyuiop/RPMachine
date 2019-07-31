package net.zyuiop.rpmachine.database.bukkitbridge;

import java.util.UUID;
import net.bridgesapi.api.BukkitBridge;
import net.zyuiop.rpmachine.database.DatabaseManager;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.database.ShopsManager;
import net.zyuiop.rpmachine.database.UUIDTranslator;

/**
 * @author zyuiop
 */
public class BukkitBridgeDatabase implements DatabaseManager {
	private BukkitBridgeUUIDTranslator translator;
	private ShopsManager manager;

	public BukkitBridgeDatabase() {

	}

	public void load() {
		translator = new BukkitBridgeUUIDTranslator(BukkitBridge.get().getUUIDTranslator());
		manager = new BukkitBridgeShops();
	}

	@Override
	public PlayerData getPlayerData(UUID uuid) {
		return new BukkitBridgePlayerData(BukkitBridge.get().getPlayerManager().getPlayerData(uuid));
	}

	@Override
	public UUIDTranslator getUUIDTranslator() {
		return translator;
	}

	@Override
	public ShopsManager getShopsManager() {
		return manager;
	}
}
