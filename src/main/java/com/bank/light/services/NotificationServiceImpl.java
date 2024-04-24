package com.bank.light.services;

import com.bank.light.domain.Notification;
import com.bank.light.interfaces.NotificationService;
import com.bank.light.interfaces.UserService;
import com.bank.light.repositories.NotificationRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Integer PAGE_SIZE = 10;

    private final UserService userService;

    private final NotificationRepository repository;

    public NotificationServiceImpl(UserService userService, NotificationRepository repository) {
        this.userService = userService;
        this.repository = repository;
    }

    public void notifyManagers(String username, String message) {
        userService.getAllManagers()
                .forEach(manager -> repository.save(new Notification(username, message, manager)));
    }

    public List<Notification> notificationList(Integer page, String username) {
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id"));
        return repository.findAllByUserUsernameOrderByOpenedAsc(username, pageRequest);
    }

    public int countByUsername(String username) {
        return repository.countByUserUsername(username);
    }

    public Long pageCount(String username) {
        return Math.round(Math.ceil(countByUsername(username) / (PAGE_SIZE * 1.0)));
    }

    public int countUnread(String username) {
        return repository.countByUserUsernameAndOpened(username, false);
    }

    public void readNotification(Long id) {
        Optional<Notification> found = repository.findById(id);
        if (found.isPresent()) {
            Notification notification = found.get();
            notification.setOpened(true);
            repository.save(notification);
        }
    }
}
