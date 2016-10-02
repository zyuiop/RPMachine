package net.zyuiop.rpmachine.database.filestorage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.google.gson.Gson;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.ShopsManager;
import net.zyuiop.rpmachine.economy.shops.AbstractShopSign;
import net.zyuiop.rpmachine.economy.shops.ShopGsonHelper;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class FileShops extends ShopsManager {

	public FileShops() throws IOException {
		File shopsDirectory = new File(RPMachine.getInstance().getDataFolder(), "shops");

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
			new Gson().toJson(sign, writer);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void load() {
		File shopsDirectory = new File(RPMachine.getInstance().getDataFolder(), "/shops/");

		shopsDirectory.mkdirs();
		shopsDirectory.mkdir();

		for (File file : shopsDirectory.listFiles()) {
			if (file.isDirectory()) { continue; }

			if (!file.getName().endsWith(".json")) { continue; }

			Gson gson = new Gson();
			Location loc = locFromString(file.getName().substring(0, -5));
			ShopGsonHelper asign = null;
			try {
				asign = gson.fromJson(new FileReader(file), ShopGsonHelper.class);
				try {
					Class<? extends AbstractShopSign> clazz = (Class<? extends AbstractShopSign>) Class.forName(asign.getClassName());
					AbstractShopSign sign = gson.fromJson(new FileReader(file), clazz);
					sign.display();
					Bukkit.getLogger().info("Loaded shop " + sign.getLocation().toString());
					signs.put(loc, sign);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			} catch (FileNotFoundException e) {
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
