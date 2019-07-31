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
	private File playersDirectory;
	private FileUUIDTranslator translator;
	private FileShops fileShops;

	public FileStorageDatabase() throws IOException {
	}

	@Override
	public void load() throws IOException {
		translator = new FileUUIDTranslator();
		fileShops = new FileShops();

		playersDirectory = new File(RPMachine.getInstance().getDataFolder(), "players");

		playersDirectory.mkdirs();
		playersDirectory.mkdir();

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
		return new PlayerFile(uuid, configuration, data);
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
