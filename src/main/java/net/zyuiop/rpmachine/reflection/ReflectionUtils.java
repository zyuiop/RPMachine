package net.zyuiop.rpmachine.reflection;

import org.bukkit.Bukkit;
import org.bukkit.Server;

/**
 * @author zyuiop
 */
public class ReflectionUtils {
	private static final String nameSpace;
	private static ReflectionFunctions version = null;

	static {
		Server server = Bukkit.getServer();
		Class<?> serverClass = server.getClass();
		String[] parts = serverClass.getName().split("\\.");
		// [org, bukkit, craftbukkit, v1..., ...]

		if (parts.length < 4) {
			Bukkit.getLogger().severe("Unknown minecraft version. Cannot load plugin.");
			Bukkit.getServer().shutdown();
			nameSpace = null;
		} else {

			nameSpace = parts[3];
			try {
				Class.forName("net.minecraft.server." + nameSpace + ".WorldServer");
				version = (ReflectionFunctions) Class.forName("net.zyuiop.rpmachine.reflection." + nameSpace).newInstance();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				Bukkit.getLogger().severe("Unknown minecraft version. Cannot load plugin.");
				Bukkit.getServer().shutdown();
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	public static ReflectionFunctions getVersion() {
		return version;
	}
}
