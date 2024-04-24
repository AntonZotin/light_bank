package com.bank.light.services;

import com.bank.light.domain.Account;
import com.bank.light.domain.Role;
import com.bank.light.domain.Transaction;
import com.bank.light.domain.User;
import com.bank.light.exceptions.BalanceNotNullException;
import com.bank.light.exceptions.UserAlreadyExistException;
import com.bank.light.exceptions.UserEditException;
import com.bank.light.exceptions.UserNotFoundException;
import com.bank.light.interfaces.RoleService;
import com.bank.light.interfaces.UserService;
import com.bank.light.models.UserDto;
import com.bank.light.models.UserEditDto;
import com.bank.light.repositories.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private static final Integer PAGE_SIZE = 10;
    private final UserRepository repository;

    private final RoleService roleService;

    private final PasswordEncoder encoder;

    protected final AuthenticationManager authenticationManager;

    public UserServiceImpl(UserRepository repository, RoleService roleService, PasswordEncoder encoder, AuthenticationManager authenticationManager) {
        this.repository = repository;
        this.roleService = roleService;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
    }

    public User get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id.toString()));
    }

    public User getByUsername(String username) {
        return repository.findByUsernameAndEnabled(username, true);
    }

    public User register(UserDto userDto) {
        if (usernameExists(userDto.getUsername())) {
            throw new UserAlreadyExistException(userDto.getUsername());
        }

        User user = new User(userDto.getUsername(), encoder.encode(userDto.getPassword()), roleService.getRoles(Role.USER));
        return repository.save(user);
    }

    public User edit(UserEditDto userEditDto) {
        if (userEditDto.getPassword() != null && !userEditDto.getPassword().equals(userEditDto.getConfirmPassword())) {
            throw new UserEditException("Password is not confirmed");
        }
        User user = get(userEditDto.getUserId());
        user.setUsername(userEditDto.getUsername());
        if (userEditDto.getPassword() != null && !userEditDto.getPassword().isEmpty())
            user.setPassword(encoder.encode(userEditDto.getPassword()));
        user.setEnabled(userEditDto.getIsEnabled());
        Set<Role> roles = new HashSet<>();
        if (userEditDto.getIsUser())
            roles.add(roleService.getRole(Role.USER));
        if (userEditDto.getIsManager())
            roles.add(roleService.getRole(Role.MANAGER));
        if (userEditDto.getIsAdmin())
            roles.add(roleService.getRole(Role.ADMIN));
        user.setRoles(roles);
        return repository.save(user);
    }

    public boolean usernameExists(String username) {
        return repository.findByUsernameAndEnabled(username, true) != null;
    }

    public Account getAccountByUsername(String username) {
        User found = repository.findByUsernameAndEnabled(username, true);
        if (found == null) {
            throw new UserNotFoundException(username);
        }
        return found.getAccount();
    }

    public void disableUser(String username) {
        User found = repository.findByUsernameAndEnabled(username, true);
        if (found == null) {
            throw new UserNotFoundException(username);
        }
        if (found.getAccount().getBalance() != 0.0) {
            throw new BalanceNotNullException();
        }
        found.setEnabled(false);
        repository.save(found);
    }

    public List<User> getAllManagers() {
        Role manager = roleService.getRole(Role.MANAGER);
        return repository.findAllByRolesContaining(manager);
    }

    public List<User> getAllUsers() {
        return repository.findAll();
    }

    public Long pageCount(String role) {
        Long count;
        if (role != null)
            count = repository.countByRolesContaining(roleService.getRole(Role.VALUE_TO_ROLE.get(role)));
        else
            count = repository.count();
        return Math.round(Math.ceil(count / (PAGE_SIZE * 1.0)));
    }

    public List<User> findAllByPage(Integer page, String role) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id"));
        if (role != null)
            return repository.findAllByRolesContaining(roleService.getRole(Role.VALUE_TO_ROLE.get(role)), pageRequest).getContent();
        else
            return repository.findAll(pageRequest).getContent();
    }
}
