package com.bank.light.security.oauth;


import com.bank.light.domain.Role;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User implements OAuth2User, Serializable {

    private final OAuth2User oauth2User;

    private final Collection<GrantedAuthority> authorities;

    public CustomOAuth2User(final OAuth2User oauth2User) {
        this.oauth2User = oauth2User;
        this.authorities = new HashSet<>(oauth2User.getAuthorities());
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getName() {
        return oauth2User.getAttribute("email");
    }

    public void addRoles(final Set<Role> grantedAuthority) {
        grantedAuthority.forEach(role -> this.authorities.add(new SimpleGrantedAuthority(role.getName())));
    }
}
