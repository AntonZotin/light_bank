package com.bank.light.repositories;

import com.bank.light.domain.Role;
import com.bank.light.domain.User;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsernameAndEnabled(String username, boolean enabled);

    List<User> findAllByRolesContaining(Role role);

    Long countByRolesContaining(Role role);

    Page<User> findAllByRolesContaining(Role role, Pageable pageable);
}
