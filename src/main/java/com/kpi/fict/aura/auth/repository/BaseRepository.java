package com.kpi.fict.aura.auth.repository;

import com.kpi.fict.aura.auth.exception.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    String getEntityName();

    default T findByIdRequired(ID id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(getEntityName(), id.toString()));
    }

}
