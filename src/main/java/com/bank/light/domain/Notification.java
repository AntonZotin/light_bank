package com.bank.light.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "notifications")
@NoArgsConstructor
@Getter
@Setter
public class Notification implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String message;

    @ManyToOne
    private User user;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean opened;

    public Notification(String username, String message, User user) {
        this.username = username;
        this.message = message;
        this.user = user;
        this.opened = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification notification = (Notification) o;
        return opened == notification.opened
                && id.equals(notification.id)
                && username.equals(notification.username)
                && message.equals(notification.message)
                && createdAt.equals(notification.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, message, createdAt, opened);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", message='" + message + '\'' +
                ", createdAt=" + createdAt +
                ", opened=" + opened +
                '}';
    }
}
