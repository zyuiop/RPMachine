package net.zyuiop.rpmachine.entities;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.projects.Project;

import java.util.function.Supplier;

/**
 * @author Louis Vialar
 */
public enum LegalEntityType {
    PLAYER(PlayerData.class, RPMachine::database, "joueur"),
    CITY(City.class, () -> RPMachine.getInstance().getCitiesManager(), "ville"),
    PROJECT(Project.class, () -> RPMachine.getInstance().getProjectsManager(), "projet"),
    ADMIN(AdminLegalEntity.class, () -> AdminLegalEntity.INSTANCE, "admin");

    private final EntityAndRepoHolder<? extends LegalEntity> holder;
    public final String name;

    <T extends LegalEntity> LegalEntityType(Class<T> clazz, Supplier<LegalEntityRepository<T>> repository, String name) {
        this.holder = new EntityAndRepoHolder<>(clazz, repository);
        this.name = name;
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
}
