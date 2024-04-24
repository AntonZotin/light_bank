package com.bank.light.interfaces;

import com.bank.light.domain.Role;
import java.util.Set;

public interface RoleService {
    Set<Role> getRoles(String roleName);

    Role getRole(String roleName);
}
