package net.zyuiop.rpmachine.entities;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.projects.Project;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Louis Vialar
 */
public enum LegalEntityType {
    PLAYER(PlayerData.class, RPMachine::database, "joueur", "me", "player", "moi"),
    CITY(City.class, () -> RPMachine.getInstance().getCitiesManager(), "ville", "city"),
    PROJECT(Project.class, () -> RPMachine.getInstance().getProjectsManager(), "projet", "project", "zone"),
    ADMIN(AdminLegalEntity.class, () -> AdminLegalEntity.INSTANCE, "admin");

    private static final Map<String, LegalEntityType> types = new HashMap<>();
    private final EntityAndRepoHolder<? extends LegalEntity> holder;
    public final String name;
    public final String[] aliases;

    <T extends LegalEntity> LegalEntityType(Class<T> clazz, Supplier<LegalEntityRepository<T>> repository, String name, String... aliases) {
        this.holder = new EntityAndRepoHolder<>(clazz, repository);
        this.name = name;
        this.aliases = aliases;
    }

    static {
        for (LegalEntityType t : LegalEntityType.values()) {
            types.put(t.name, t);
            for (String alias : t.aliases)
                types.put(alias, t);
        }
    }

    public static LegalEntityType get(LegalEntity entity) {
        for (LegalEntityType value : values())
            if (value.holder.clazz.isInstance(entity))
                return value;

        throw new NoClassDefFoundError();
    }

    public <T extends LegalEntity> EntityAndRepoHolder<T> holder() {
        return (EntityAndRepoHolder<T>) holder;
    }

    public static class EntityAndRepoHolder<T extends LegalEntity> {
        public final Class<T> clazz;
        public final Supplier<LegalEntityRepository<T>> repository;

        private EntityAndRepoHolder(Class<T> clazz, Supplier<LegalEntityRepository<T>> repository) {
            this.clazz = clazz;
            this.repository = repository;
        }
    }

    public static Set<String> getAliases() {
        return types.keySet();
    }

    public static @Nonnull LegalEntity getLegalEntity(Player p, String defaultParam, String... args) throws CommandException {
        if (args.length == 0 && defaultParam == null) {
            throw new CommandException("Format incorrect.");
        } else {
            String type, name;

            if (args.length > 1) {
                type = args[0];
                name = args[1];
            } else if (args.length > 0 && defaultParam != null && !getAliases().contains(args[0].toLowerCase())) {
                type = defaultParam;
                name = args[0];
            } else {
                type = args[0];
                name = null;
            }

            LegalEntityType t = types.get(type.toLowerCase());

            if (t == null) {
                throw new CommandException("Type d'entité non reconnu: " + type);
            } else {
                switch (t) {
                    case PLAYER:
                        Player target = name == null ? p : Bukkit.getPlayerExact(name);

                        if (target == null)
                            throw new CommandException("Joueur introuvable : " + name);

                        return RPMachine.database().getPlayerData(target);
                    case CITY:
                        City city = name == null ? RPMachine.getInstance().getCitiesManager().getPlayerCity(p) : RPMachine.getInstance().getCitiesManager().getCity(name);

                        if (city == null && name == null)
                            throw new CommandException("Vous n'êtes membre d'aucune ville, merci de préciser le nom de la ville ciblée.");
                        else if (city == null)
                            throw new CommandException("Ville introuvable : " + name);
                        else
                            return city;
                    case PROJECT:
                        if (name == null)
                            throw new CommandException("Nom du projet manquant.");

                        Project project = RPMachine.getInstance().getProjectsManager().getZone(name);
                        if (project == null)
                            throw new CommandException("Projet introuvable : " + name);
                        return project;
                    case ADMIN:
                        return AdminLegalEntity.INSTANCE;
                }
            }
        }

        throw new CommandException("Erreur d'exécution.");
    }
}
