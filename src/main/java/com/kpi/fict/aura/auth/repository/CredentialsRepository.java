package com.kpi.fict.aura.auth.repository;

import com.kpi.fict.aura.auth.model.Credentials;
import com.kpi.fict.aura.auth.model.User;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

public interface CredentialsRepository extends BaseRepository<Credentials, Long> {

    @Override
    default String getEntityName() {
        return Credentials.class.getSimpleName();
    }

    Optional<Credentials> findByUser(User user);

    @EntityGraph(attributePaths = { "user" })
    Optional<Credentials> findWithUserById(Long id);

    @EntityGraph(attributePaths = { "user", "user.userRoles" })
    Optional<Credentials> findWithUserAndRolesByUsername(String username);

}