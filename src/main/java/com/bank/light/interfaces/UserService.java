package com.bank.light.interfaces;

import com.bank.light.domain.Account;
import com.bank.light.domain.User;
import com.bank.light.models.UserDto;
import com.bank.light.models.UserEditDto;
import java.util.List;

public interface UserService {
    User get(Long id);

    User getByUsername(String username);

    User register(UserDto userDto);

    User edit(UserEditDto userEditDto);

    boolean usernameExists(String username);

    Account getAccountByUsername(String username);

    void disableUser(String username);

    List<User> getAllManagers();

    List<User> getAllUsers();

    Long pageCount(String role);

    List<User> findAllByPage(Integer page, String role);
}

