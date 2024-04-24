package com.bank.light.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
@Entity
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @OneToOne(cascade = CascadeType.ALL)
    private Account account;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    private boolean enabled;

    public User(String username, String password, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.account = new Account();
        this.roles = roles;
        this.enabled = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return enabled == user.enabled
                && id.equals(user.id)
                && username.equals(user.username)
                && password.equals(user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, enabled);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", account=" + account +
                ", roles=" + roles +
                ", enabled=" + enabled +
                '}';
    }

    public String rolesToString() {
        return this.roles.stream().map(r -> Role.ROLE_TO_VALUE.get(r.getName())).collect(Collectors.joining(", "));
    }

    public boolean isUser() {
        return rolesToString().contains(Role.USER_VALUE);
    }

    public boolean isManager() {
        return rolesToString().contains(Role.MANAGER_VALUE);
    }

    public boolean isAdmin() {
        return rolesToString().contains(Role.ADMIN_VALUE);
    }
}
