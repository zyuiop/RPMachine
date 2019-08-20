package net.zyuiop.rpmachine.database;

import net.zyuiop.rpmachine.entities.LegalEntityRepository;
import net.zyuiop.rpmachine.shops.ShopsManager;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * @author zyuiop
 */
public interface DatabaseManager extends LegalEntityRepository<PlayerData> {
	void load() throws IOException;

	PlayerData getPlayerData(UUID uuid);
	
	default PlayerData getPlayerData(Player player) {
		return getPlayerData(player.getUniqueId());
	}

	List<PlayerData> getPlayers();

	List<PlayerData> getPlayers(Predicate<PlayerData> filter);

	UUIDTranslator getUUIDTranslator();
}
