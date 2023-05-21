package pro.sky.telegrambot.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Entity
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long chatId;

    private String task;

    private LocalDateTime sendDateTime;

    public NotificationTask(long chatId) {
        this.chatId = chatId;
    }

    public NotificationTask() {

    }

    public boolean createNotification(String text) {
        Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}) (.+)");
        Matcher matcher = pattern.matcher(text);

        if (!matcher.matches())
            return false;

        LocalDateTime sendDateTime = LocalDateTime.parse(matcher.group(1), DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        if (sendDateTime.isBefore(LocalDateTime.now())) {
            return false;
        }

        this.sendDateTime = sendDateTime;
        this.task = matcher.group(2);
        return true;
    }

    public Long getId() {
        return id;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public LocalDateTime getSendDateTime() {
        return sendDateTime;
    }

    public void setSendDateTime(LocalDateTime sendDateTime) {
        this.sendDateTime = sendDateTime;
    }

    @Override
    public int hashCode() {
        return (int) (long) id * this.chatId.hashCode() * this.task.hashCode() * this.sendDateTime.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof NotificationTask))
            return false;
        NotificationTask other = (NotificationTask) obj;
        if (this.id.equals(other.id))
            return true;
        return this.task.equals(other.task) &&
                this.chatId.equals(other.chatId) &&
                this.sendDateTime.equals(other.sendDateTime);
    }

    @Override
    public String toString() {
        return "NotificationTask{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", task='" + task + '\'' +
                ", sendDateTime=" + sendDateTime +
                '}';
    }
}
