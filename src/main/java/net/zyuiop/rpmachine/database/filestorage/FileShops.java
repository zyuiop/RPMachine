package net.zyuiop.rpmachine.database.filestorage;

import com.google.gson.Gson;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.ShopsManager;
import net.zyuiop.rpmachine.economy.shops.AbstractShopSign;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.*;

public class FileShops extends ShopsManager {
	private final File shopsDirectory = new File(RPMachine.getInstance().getDataFolder(), "shops");

	public FileShops() throws IOException {
		shopsDirectory.mkdirs();
		shopsDirectory.mkdir();

		if (!shopsDirectory.isDirectory()) {
			throw new IOException("Error : shops directory is not a directory at " + shopsDirectory.getAbsolutePath());
		}

		load();
	}

	@Override
	protected void doCreate(AbstractShopSign sign) {
		String target = locAsString(sign.getLocation());
		File shopsDirectory = new File(RPMachine.getInstance().getDataFolder().getPath() + "/shops/");
		File file = new File(shopsDirectory, target);
		if (file.exists()) {
			try {
				FileUtils.moveFile(file, new File(shopsDirectory, target + "@" + System.currentTimeMillis() + ".json.backup"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		write(file, sign);
	}

	private void write(File file, AbstractShopSign sign) {
		try {
			FileWriter writer = new FileWriter(file);
			gson.toJson(sign, AbstractShopSign.class, writer);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void load() {
		RPMachine.getInstance().getLogger().info("Loading shops...");

		for (File file : shopsDirectory.listFiles()) {
			RPMachine.getInstance().getLogger().info("... " + file);
			if (file.isDirectory()) {
				continue;
			}

			try {
				Location loc = locFromString(file.getName());

				AbstractShopSign sign = gson.fromJson(new FileReader(file), AbstractShopSign.class);
				sign.display();
				Bukkit.getLogger().info("Loaded shop " + sign.getLocation().toString());
				signs.put(loc, sign);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void doRemove(AbstractShopSign shopSign) {
		String target = locAsString(shopSign.getLocation());
		File shopsDirectory = new File(RPMachine.getInstance().getDataFolder().getPath() + "/shops/");
		File file = new File(shopsDirectory, target);
		if (file.exists()) {
			try {
				FileUtils.moveFile(file, new File(shopsDirectory, target + "@" + System.currentTimeMillis() + ".json.removed"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void save(AbstractShopSign shopSign) {
		String target = locAsString(shopSign.getLocation());
		File shopsDirectory = new File(RPMachine.getInstance().getDataFolder().getPath() + "/shops/");
		File file = new File(shopsDirectory, target);
		if (!file.exists()) {
			return;
		}

		write(file, shopSign);
	}
}
