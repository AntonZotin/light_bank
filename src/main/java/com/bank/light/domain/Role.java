package com.bank.light.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@Getter
@Setter
public class Role implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public Role(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(id, role.id)
                && Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public static final String ADMIN = "ROLE_ADMIN";
    public static final String ADMIN_VALUE = "Admin";
    public static final String MANAGER = "ROLE_MANAGER";
    public static final String MANAGER_VALUE = "Manager";
    public static final String USER = "ROLE_USER";
    public static final String USER_VALUE = "User";

    public static final Map<String, String> VALUE_TO_ROLE = Map.of(ADMIN_VALUE, ADMIN, MANAGER_VALUE, MANAGER, USER_VALUE, USER);

    public static final Map<String, String> ROLE_TO_VALUE = Map.of(ADMIN, ADMIN_VALUE, MANAGER, MANAGER_VALUE, USER, USER_VALUE);
}
