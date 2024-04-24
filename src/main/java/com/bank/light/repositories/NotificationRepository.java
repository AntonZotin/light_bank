package com.bank.light.repositories;

import com.bank.light.domain.Notification;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserUsernameOrderByOpenedAsc(String username, Pageable pageable);

    int countByUserUsername(String username);

    int countByUserUsernameAndOpened(String username, boolean opened);
}
