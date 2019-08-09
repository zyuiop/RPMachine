package net.zyuiop.rpmachine.database.filestorage;

import com.google.gson.JsonSyntaxException;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.StoredEntity;
import net.zyuiop.rpmachine.json.Json;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.logging.Level;

/**
 * @author Louis Vialar
 */
public abstract class FileEntityStore<T extends StoredEntity> {
    private final Class<T> tClass;
    private final String prefix;
    private final File folder;

    public FileEntityStore(Class<T> tClass, String prefix) {
        this.tClass = tClass;
        this.prefix = prefix;
        this.folder = new File(RPMachine.getInstance().getDataFolder().getPath() + "/" + prefix);
    }

    /**
     * Called when an entity was just loaded, to add it to the overriding class datastore
     */
    protected abstract void loadedEntity(T entity);

    protected void load() {
        // Create the directory (does nothing if it exists)
        folder.mkdirs();
        folder.mkdir();

        // We can't continue without the folder
        if (!folder.isDirectory())
            throw new RuntimeException(prefix + " folder doesn't exist. " + folder);

        for (File file : folder.listFiles()) {
            try {
                if (file.getName().endsWith(".removed"))
                    continue; // Skip removed files

                // Read the file to GSON
                BufferedReader reader = new BufferedReader(new FileReader(file));
                T entity = Json.GSON.fromJson(reader, tClass);
                entity.setFileName(file.getName()); // Update the filename if the file moved

                // Dispatch the read entity
                loadedEntity(entity);
                RPMachine.getInstance().getLogger().info("Loaded " + prefix + " " + file.getName());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JsonSyntaxException e) {
                RPMachine.getInstance().getLogger().log(Level.SEVERE, "Cannot load " + file.getName(), e);
            }
        }
    }

    protected boolean createEntity(String fileName, T entity) {
        File file = new File(folder, fileName + ".json");

        // Try to iterate to find a new name
        if (file.exists()) {
            int i = 1;
            while (i <= 100 && file.exists()) {
                file = new File(folder, fileName + "(" + i + ").json");
                i++;
            }

            if (file.exists())
                return false;
        }

        // Create the file
        try {
            boolean create = file.createNewFile();
            if (!create)
                return false;

            // Set the filename to the entity and load it
            entity.setFileName(file.getName());
            loadedEntity(entity);

            // Save the entity to the created file
            saveEntity(entity);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected void saveEntity(T entity) {
        Bukkit.getScheduler().runTaskAsynchronously(RPMachine.getInstance(), () -> {
            File file = new File(folder, entity.getFileName());

            // Fail if the file doesn't exist
            if (!file.exists())
                throw new RuntimeException("File not found " + file.getAbsolutePath());

            BufferedWriter writer = null;
            try {
                // Write to the buffer directly from GSon
                writer = new BufferedWriter(new FileWriter(file));
                Json.GSON.toJson(entity, tClass, writer);
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    protected void removeEntity(T entity) {
        File file = new File(folder, entity.getFileName());

        RPMachine.getInstance().getLogger().info("Removing " + prefix + " / " + entity.getFileName());

        if (file.exists()) {
            try {
                FileUtils.moveFile(file, new File(folder, file.getName() + "@" + System.currentTimeMillis() + ".removed"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
