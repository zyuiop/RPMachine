package net.zyuiop.rpmachine.database;

/**
 * Something that can be stored (as JSon)
 * @author Louis Vialar
 */
public interface StoredEntity {
    String getFileName();

    void setFileName(String name);
}
