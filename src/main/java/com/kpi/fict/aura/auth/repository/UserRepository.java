package com.kpi.fict.aura.auth.repository;

import com.kpi.fict.aura.auth.model.User;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface UserRepository extends BaseRepository<User, Long> {

    @Override
    default String getEntityName() {
        return User.class.getSimpleName();
    }

    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = { "userRoles", "userRoles.role" })
    Optional<User> findWithRolesById(Long userId);

}