package net.zyuiop.rpmachine.settings;

public enum PlotMessagesSettings implements SettingValue {
    OFF("désactivés"), ACTION_BAR("dans la barre d'action"), CHAT("dans le chat");

    private final String description;
    public static final String KEY = "plot_messages";

    PlotMessagesSettings(String description) {
        this.description = description;
    }

    @Override
    public String description() {
        return description;
    }
}
