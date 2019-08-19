package net.zyuiop.rpmachine.database.filestorage;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.DatabaseManager;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.database.UUIDTranslator;
import net.zyuiop.rpmachine.entities.LegalEntityRepository;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author zyuiop
 */
public class FileStorageDatabase implements DatabaseManager, LegalEntityRepository<PlayerData> {
    private File playersDirectory;
    private FileUUIDTranslator translator;
    private Map<UUID, PlayerData> playerFileMap = new HashMap<>();

    public FileStorageDatabase() throws IOException {
    }

    @Override
    public void load() throws IOException {
        translator = new FileUUIDTranslator();

        playersDirectory = new File(RPMachine.getInstance().getDataFolder(), "players");

        playersDirectory.mkdirs();
        playersDirectory.mkdir();

        if (!playersDirectory.isDirectory()) {
            throw new IOException("Error : players directory is not a directory at " + playersDirectory.getAbsolutePath());
        }
    }

    @Override
    public PlayerData getPlayerData(UUID uuid) {
        if (!playerFileMap.containsKey(uuid)) {
            File data = new File(playersDirectory, uuid.toString() + ".yml");
            if (!data.exists()) {
                try {
                    data.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(data);
            playerFileMap.put(uuid, new PlayerData(uuid, configuration, data));
        }

        return playerFileMap.get(uuid);
    }

    @Override
    public UUIDTranslator getUUIDTranslator() {
        return translator;
    }

    @Override
    public PlayerData findEntity(String tag) {
        return getPlayerData(UUID.fromString(tag));
    }

    @Override
    public String getTag(PlayerData entity) {
        return entity.getUuid().toString();
    }
}
