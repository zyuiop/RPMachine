package net.zyuiop.rpmachine.entities;

import java.util.Map;

/**
 * @author Louis Vialar
 */
public interface LegalEntityRepository<T extends LegalEntity> {
    T findEntity(String tag);

    String getTag(T entity);
}
