package net.zyuiop.rpmachine.settings;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.CompoundCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.utils.Symbols;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class SettingsCommand extends CompoundCommand {
    public SettingsCommand() {
        super("settings", null);

        for (Settings value : Settings.values()) {
            addSetting(value);
        }
        registerHelp();
    }

    private void addSetting(Settings setting) {
        registerSubCommand(setting.name().toLowerCase(), new SubCommand() {
            @Override
            public String getUsage() {
                return "[nouvelle valeur]";
            }

            @Override
            public String getDescription() {
                return setting.description;
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                if (args.length == 0) {
                    var settingValue = RPMachine.getPlayerData(sender).getSetting(setting);
                    sender.sendMessage(ChatColor.YELLOW + StringUtils.capitalize(setting.description) + ChatColor.GRAY + " : " + ChatColor.GOLD + settingValue.description());
                    sender.sendMessage(ChatColor.GRAY + "Valeurs possibles : (cliquez pour changer)");

                    for (SettingValue value : setting.possibleValues()) {
                        TextComponent tc = new TextComponent(Symbols.ARROW_RIGHT_FULL + " " + StringUtils.capitalize(value.description()));
                        tc.setColor(settingValue == value ? net.md_5.bungee.api.ChatColor.GREEN : net.md_5.bungee.api.ChatColor.YELLOW);
                        tc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command + " " + subCommand + " " + value.name()));
                        sender.sendMessage(tc);
                    }
                    return true;
                } else {
                    var value = setting.parse(args[0], null);

                    if (value == null) {
                        return run(sender, command, subCommand, new String[0]);
                    } else {
                        RPMachine.getPlayerData(sender).setSetting(setting, value);
                        sender.sendMessage(ChatColor.YELLOW + StringUtils.capitalize(setting.description) + ChatColor.GRAY + " : " + ChatColor.GOLD + value.description());
                        return true;
                    }
                }
            }
        });
    }
}
