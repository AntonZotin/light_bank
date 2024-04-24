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

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleRepository roleRepo;

    public RoleServiceImpl(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    public Set<Role> getRoles(String roleName) {
        Role user = getRole(Role.USER);
        Role manager = getRole(Role.MANAGER);
        Role admin = getRole(Role.ADMIN);
        if (roleName.equals(Role.ADMIN))
            return Set.of(user, manager, admin);
        else if (roleName.equals(Role.MANAGER))
            return Set.of(user, manager);
        else return Set.of(user);
    }

    public Role getRole(String roleName) {
        Role role = roleRepo.findByName(roleName);
        if (role == null) {
            role = roleRepo.save(new Role(roleName));
            log.info("Add role %s".formatted(role));
        }
        return role;
    }
}
