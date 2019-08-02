package net.zyuiop.rpmachine.database;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.zyuiop.rpmachine.RPMachine;
import org.bukkit.Bukkit;

/**
 * Originally from JedisBungee by minecrafter : https://github.com/minecrafter/RedisBungee
 */
public abstract class UUIDTranslatorBase implements UUIDTranslator {
	private final Pattern UUID_PATTERN = Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}");
	private final Pattern MOJANGIAN_UUID_PATTERN = Pattern.compile("[a-fA-F0-9]{32}");
	private final Map<String, CachedUUIDEntry> nameToUuidMap = new ConcurrentHashMap<>(128, 0.5f, 4);
	private final Map<UUID, CachedUUIDEntry> uuidToNameMap = new ConcurrentHashMap<>(128, 0.5f, 4);

	private static UUID convertUUID(String id) {
		return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" + id.substring(20, 32));
	}

	protected abstract CachedUUIDEntry getSavedUUID(String name);

	protected abstract CachedUUIDEntry getSavedName(UUID uuid);

	protected abstract void removeFromCache(UUID uuid);

	protected abstract void removeFromCache(String string);

	protected abstract void save(String name, UUID uuid);

	@Override
	public void cachePair(UUID uuid, String playerName) {
		this.save(playerName, uuid);
	}

	@Override
	public UUID getUUID(String name) {
		// If the player is online, give them their UUID.
		// Remember, local data > remote data.
		if (Bukkit.getPlayer(name) != null) { return Bukkit.getPlayer(name).getUniqueId(); }

		// Check if it exists in the map
		CachedUUIDEntry cachedUUIDEntry = nameToUuidMap.get(name.toLowerCase());
		if (cachedUUIDEntry != null) {
			if (!cachedUUIDEntry.expired()) { return cachedUUIDEntry.getUuid(); } else {
				nameToUuidMap.remove(name);
			}
		}

		// Check if we can exit early
		if (UUID_PATTERN.matcher(name).find()) {
			return UUID.fromString(name);
		}

		if (MOJANGIAN_UUID_PATTERN.matcher(name).find()) {
			// Reconstruct the UUID
			return convertUUID(name);
		}

		// Let's try the DB.
		CachedUUIDEntry entry = getSavedUUID(name.toLowerCase());

		if (entry != null) {
			// Check for expiry:
			if (entry.expired()) {
				removeFromCache(name.toLowerCase());
			} else {
				nameToUuidMap.put(name.toLowerCase(), entry);
				uuidToNameMap.put(entry.getUuid(), entry);
				return entry.getUuid();
			}
		}

		return null; // Nope, game over!
	}

	@Override
	public String getName(UUID uuid) {
		if (Bukkit.getPlayer(uuid) != null) {
			return Bukkit.getPlayer(uuid).getName();
		}

		// Check if it exists in the map
		CachedUUIDEntry cachedUUIDEntry = uuidToNameMap.get(uuid);
		if (cachedUUIDEntry != null) {
			if (!cachedUUIDEntry.expired()) { return cachedUUIDEntry.getName(); } else {
				uuidToNameMap.remove(uuid);
			}
		}

		// Okay, it wasn't locally cached. Let's try DB.
		CachedUUIDEntry entry = getSavedName(uuid);
		if (entry != null) {

			// Check for expiry:
			if (entry.expired()) {
				removeFromCache(uuid);
			} else {
				nameToUuidMap.put(entry.getName().toLowerCase(), entry);
				uuidToNameMap.put(uuid, entry);
				return entry.getName();
			}
		}

		return null;
	}

	public static class CachedUUIDEntry {
		private final String name;
		private final UUID uuid;

		public CachedUUIDEntry(String name, UUID uuid) {
			this.name = name;
			this.uuid = uuid;
		}

		public boolean expired() {
			return false;
			// Never expire UUID entries
		}

		public String getName() {
			return name;
		}

		public UUID getUuid() {
			return uuid;
		}
	}
}
