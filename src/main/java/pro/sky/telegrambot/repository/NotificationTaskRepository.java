package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {
    Optional<List<NotificationTask>> findAllBySendDateTime(LocalDateTime localDateTime);
}
