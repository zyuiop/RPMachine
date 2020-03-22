package net.zyuiop.rpmachine.settings;

import java.lang.reflect.InvocationTargetException;

public enum Settings {
    PLOT_MESSAGES(PlotMessagesSettings.class, PlotMessagesSettings.CHAT, "affichage des messages d'entrée/sortie de parcelle et de ville");

    private final Class<? extends Enum<?>> clazz;
    public final SettingValue defaultValue;
    public final String description;

    <T extends Enum<T>> Settings(Class<T> clazz, SettingValue defaultValue, String description) {
        this.clazz = clazz;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public <T extends SettingValue> T parse(String value) {
        return parse(value, (T) this.defaultValue);
    }

    public <T extends SettingValue> T parse(String value, T defaultValue) {
        try {
            return (T) clazz.getMethod("valueOf", Class.class, String.class)
                    .invoke(null, clazz, value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
        return defaultValue;
    }

    public SettingValue[] possibleValues() {
        return (SettingValue[]) clazz.getEnumConstants();
    }

}
