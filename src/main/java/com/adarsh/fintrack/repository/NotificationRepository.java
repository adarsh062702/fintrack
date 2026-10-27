package com.adarsh.fintrack.repository;

import com.adarsh.fintrack.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
