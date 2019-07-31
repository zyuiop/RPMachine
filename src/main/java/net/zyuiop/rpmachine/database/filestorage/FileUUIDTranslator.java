package net.zyuiop.rpmachine.database.filestorage;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.UUIDTranslatorBase;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author zyuiop
 */
public class FileUUIDTranslator extends UUIDTranslatorBase {
	private final YamlConfiguration uuidsConf;
	private final YamlConfiguration namesConf;
	private final File uuidsFile;
	private final File namesFile;

	public FileUUIDTranslator() {
		uuidsFile = new File(RPMachine.getInstance().getDataFolder().getPath() + "/uuids.yaml");
		namesFile = new File(RPMachine.getInstance().getDataFolder().getPath() + "/names.yaml");

		try {
			if (!uuidsFile.exists()) { uuidsFile.createNewFile(); }

			if (!namesFile.exists()) { namesFile.createNewFile(); }
		} catch (IOException e) {
			e.printStackTrace();
		}

		uuidsConf = YamlConfiguration.loadConfiguration(uuidsFile);
		namesConf = YamlConfiguration.loadConfiguration(namesFile);
	}

	private static Calendar getExpiry(long time) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date(time
		));
		calendar.add(Calendar.DAY_OF_MONTH, 7);
		return calendar;
	}

	@Override
	protected CachedUUIDEntry getSavedUUID(String name) {
		ConfigurationSection section = uuidsConf.getConfigurationSection(name);
		if (section == null) {
			return null;
		}
		return new CachedUUIDEntry(name, UUID.fromString(section.getString("uuid")), getExpiry(section.getLong("date")));
	}

	@Override
	protected CachedUUIDEntry getSavedName(UUID uuid) {
		ConfigurationSection section = namesConf.getConfigurationSection(uuid.toString());
		if (section == null) {
			return null;
		}
		return new CachedUUIDEntry(section.getString("name"), uuid, getExpiry(section.getLong("date")));
	}

	@Override
	protected void removeFromCache(UUID uuid) {
		uuidsConf.set(uuid.toString(), null);
		try {
			uuidsConf.save(uuidsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void removeFromCache(String string) {
		namesConf.set(string, null);
		try {
			namesConf.save(namesFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void save(String name, UUID uuid) {
		namesConf.set(name + ".uuid", uuid.toString());
		namesConf.set(name + ".date", System.currentTimeMillis());

		uuidsConf.set(uuid.toString() + ".name", name);
		uuidsConf.set(uuid.toString() + ".date", System.currentTimeMillis());

		try {
			namesConf.save(namesFile);
			uuidsConf.save(uuidsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
