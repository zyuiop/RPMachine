package net.zyuiop.rpmachine.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.entities.AdminLegalEntity;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.LegalEntityType;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * @author zyuiop
 */
public class CommandActAs extends CompoundCommand {
	public CommandActAs() {
		super("actas", null);

		registerSubCommand(LegalEntityType.PLAYER, (player, src) -> Optional.of(RPMachine.database().getPlayerData(player.getUniqueId())), "me");

		registerSubCommand(LegalEntityType.CITY, "",
				p -> RPMachine.getInstance().getCitiesManager().getPlayerCity(p) != null,
				(p, opt) -> Optional.ofNullable(RPMachine.getInstance().getCitiesManager().getPlayerCity(p)),
				(p, city) -> city.canActAs(p),
				"city");

		registerSubCommand(LegalEntityType.PROJECT, "<project>",
				p -> true,
				(p, opt) -> opt.flatMap((String name) -> Optional.ofNullable(RPMachine.getInstance().getProjectsManager().getZone(name))),
				(p, project) -> project.canActAsProject(p),
				"project");

		registerSubCommand(LegalEntityType.ADMIN, "",
				p -> p.hasPermission("actas.actAsAdmin"),
				(p, opt) -> Optional.of(AdminLegalEntity.INSTANCE),
				(p, project) -> true,
				"admin");
	}

    private <T extends LegalEntity> void registerSubCommand(LegalEntityType type,
                                                            BiFunction<Player, Optional<String>, Optional<T>> entityFetcher,
                                                            String... aliases) {
        registerSubCommand(type, "", u -> true, entityFetcher, (u, v) -> true, aliases);
    }

    private <T extends LegalEntity> void registerSubCommand(LegalEntityType type,
                                                            String usage,
                                                            Predicate<Player> canExecute,
                                                            BiFunction<Player, Optional<String>, Optional<T>> entityFetcher,
                                                            BiPredicate<Player, T> entityChecker,
                                                            String... aliases) {

        registerSubCommand(type.name, new SubCommand() {
            @Override
            public String getUsage() {
                return usage;
            }

            @Override
            public boolean canUse(Player player) {
                return canExecute.test(player);
            }

            @Override
            public String getDescription() {
                return "permet d'agir en tant que " + type.name;
            }

            @Override
            public boolean run(Player sender, String[] args) {
                Optional<String> arg = args.length > 0 ? Optional.of(args[0]) : Optional.empty();
                Optional<T> result = entityFetcher.apply(sender, arg);

                if (result.isPresent()) {
                    T token = result.get();
                    if (entityChecker.test(sender, token)) {
                        RPMachine.setPlayerRoleToken(sender, token);
                        sender.sendMessage(ChatColor.GREEN + "Vous agissez désormais en tant que : " + token.displayable());
                    } else {
                        sender.sendMessage(ChatColor.RED + "Vous ne pouvez pas incarner " + token.displayable());
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Aucune entité correspondante.");
                }
                return true;
            }
        }, aliases);
    }

}
