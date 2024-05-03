package com.bank.light.security.oauth;


import com.bank.light.domain.User;
import com.bank.light.interfaces.UserService;
import com.bank.light.models.UserDto;
import java.util.UUID;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService service;

    public CustomOAuth2UserService(final UserService service) {
        this.service = service;
    }

    @Override
    public OAuth2User loadUser(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        final OAuth2User user = super.loadUser(userRequest);
        final CustomOAuth2User newUser = new CustomOAuth2User(user);
        User exist = service.getByUsername(newUser.getName());
        if (exist == null) {
            final String password = UUID.randomUUID().toString();
            exist = service.register(new UserDto(newUser.getName(), password, password));
        }
        newUser.addRoles(exist.getRoles());
        return newUser;
    }

}
