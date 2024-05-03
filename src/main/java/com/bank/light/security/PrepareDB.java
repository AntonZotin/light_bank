package com.bank.light.security;

import com.bank.light.domain.Role;
import com.bank.light.domain.User;
import com.bank.light.interfaces.RoleService;
import com.bank.light.repositories.UserRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PrepareDB {

    private static final Logger log = LoggerFactory.getLogger(PrepareDB.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepo, RoleService roleService, PasswordEncoder encoder) {
        return args -> {
            final Set<Role> userRole = Set.of(roleService.getRole(Role.USER));
            final Set<Role> managerRole = Set.of(roleService.getRole(Role.MANAGER));
            final Set<Role> adminRole = Set.of(roleService.getRole(Role.ADMIN));
            final String password = encoder.encode("password");
            if (userRepo.count() == 0) {
                print(userRepo.save(new User("user1", password, userRole)));
                print(userRepo.save(new User("user2", password, userRole)));
                print(userRepo.save(new User("user3", password, userRole)));
                print(userRepo.save(new User("manager1", password, managerRole)));
                print(userRepo.save(new User("manager2", password, managerRole)));
                print(userRepo.save(new User("admin1", password, adminRole)));
            }
        };
    }

    private void print(final User user) {
        log.info("Add {}", user);
    }
}
