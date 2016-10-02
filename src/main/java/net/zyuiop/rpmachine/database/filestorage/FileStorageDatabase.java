package net.zyuiop.rpmachine.database.filestorage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.DatabaseManager;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.database.ShopsManager;
import net.zyuiop.rpmachine.database.UUIDTranslator;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author zyuiop
 */
public class FileStorageDatabase implements DatabaseManager {
	private final File playersDirectory;
	private final FileUUIDTranslator translator = new FileUUIDTranslator();
	private final FileShops fileShops = new FileShops();

	public FileStorageDatabase() throws IOException {
		playersDirectory = new File(RPMachine.getInstance().getDataFolder().getPath() + "/players/");

		if (!playersDirectory.exists()) {
			playersDirectory.mkdirs();
		}

		if (!playersDirectory.isDirectory()) {
			throw new IOException("Error : players directory is not a directory at " + playersDirectory.getAbsolutePath());
		}
	}

	@Override
	public PlayerData getPlayerData(UUID uuid) {
		File data = new File(playersDirectory, uuid.toString() + ".yml");
		if (!data.exists())
			try {
				data.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

		YamlConfiguration configuration = YamlConfiguration.loadConfiguration(data);
		return new PlayerFile(configuration, data);
	}

	@Override
	public UUIDTranslator getUUIDTranslator() {
		return translator;
	}

	@Override
	public ShopsManager getShopsManager() {
		return fileShops;
	}
}
