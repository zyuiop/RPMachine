package net.zyuiop.rpmachine.database.filestorage;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.DatabaseManager;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.database.UUIDTranslator;
import net.zyuiop.rpmachine.entities.LegalEntityRepository;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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


        Bukkit.getLogger().info("Loading all player files!");
        // Load all players :)
        for (File f : Objects.requireNonNull(playersDirectory.listFiles((dir, name) -> name.endsWith(".yml")))) {
            String name = f.getName().substring(0, f.getName().length() - 4);
            try {
                UUID uuid = UUID.fromString(name);
                YamlConfiguration configuration = YamlConfiguration.loadConfiguration(f);
                playerFileMap.put(uuid, new PlayerData(uuid, configuration, f));
            } catch (IllegalArgumentException ignored) {
                Bukkit.getLogger().warning("IllegalArgumentException while loading " + f.getAbsolutePath());
            }
        }

        Bukkit.getLogger().info("Loaded " + playerFileMap.size() + " player files!");
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
            } else {
                Bukkit.getLogger().warning("PlayerData file for " + uuid + " was not loaded before...");
            }

            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(data);
            playerFileMap.put(uuid, new PlayerData(uuid, configuration, data));
        }

        return playerFileMap.get(uuid);
    }

    @Override
    public List<PlayerData> getPlayers() {
        return getPlayers(u -> true);
    }

    @Override
    public List<PlayerData> getPlayers(Predicate<PlayerData> filter) {
        return playerFileMap.values().stream().filter(filter).collect(Collectors.toList());
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
