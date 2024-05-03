package com.bank.light.services;

import com.bank.light.domain.Role;
import com.bank.light.interfaces.RoleService;
import com.bank.light.repositories.RoleRepository;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger LOG = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleRepository roleRepo;

    public RoleServiceImpl(final RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    public Set<Role> getRoles(final String roleName) {
        final Role user = getRole(Role.USER);
        final Role manager = getRole(Role.MANAGER);
        final Role admin = getRole(Role.ADMIN);
        return switch (roleName) {
            case Role.ADMIN -> Set.of(user, manager, admin);
            case Role.MANAGER -> Set.of(user, manager);
            default -> Set.of(user);
        };
    }

    public Role getRole(final String roleName) {
        Role role = roleRepo.findByName(roleName);
        if (role == null) {
            role = roleRepo.save(new Role(roleName));
            LOG.info("Add role: {}", role);
        }
        return role;
    }
}
