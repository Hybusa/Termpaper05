package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.enums.MessageState;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.service.NotificationTaskService;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;

    private final NotificationTaskService notificationTaskService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            String messageText = update.message().text();
            MessageState messageState = getState(messageText);
            switch (messageState) {
                case START:
                    startBot(update.message().chat().id(), update.message().chat().firstName());
                    logger.info("Start Initiated");
                    break;
                case NOTIFY_TASK:
                    createNotification(update.message().chat().id(), messageText);
                    logger.info("Notification for chat id: " + update.message().chat().id() + "added");
                    break;
                default:
                    logger.info("Unexpected");
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void createNotification(long chatId, String messageText) {
        SendMessage message;
        NotificationTask task = new NotificationTask(chatId);

        if (!task.createNotification(messageText)) {
            logger.info("Notification not saved");
            message = new SendMessage(chatId, "Notification NOT Added! Check Formatting or Date");
        } else {
            message = checkMessage(chatId, task);
        }
        sendMessage(message);
    }

    private SendMessage checkMessage(long chatId, NotificationTask task) {

        if (notificationTaskService.saveTask(task) == null) {
            logger.error("Notification NOT saved");
            return new SendMessage(chatId, "Notification was not able to be saved!");
        } else {
            logger.info("Notification saved");
            return new SendMessage(chatId, "Notification Added!");
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    private void checkAndSend() {
        Optional<List<NotificationTask>> optionalNotificationsToSend = notificationTaskService
                .findNotifications(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        if (optionalNotificationsToSend.isEmpty())
            return;
        List<NotificationTask> notificationsToSend = optionalNotificationsToSend.get();
        for (NotificationTask notificationTask : notificationsToSend) {
            SendMessage message = new SendMessage(notificationTask.getChatId(), notificationTask.getTask());
            sendMessage(message);
            notificationTaskService.deleteNotificationById(notificationTask.getId());
        }
    }

    private void sendMessage(SendMessage message) {
        SendResponse response = telegramBot.execute(message);
        if (response.isOk()) {
            logger.info("Message: {} sent", message);
        } else {
            logger.error("Error sending. Code: " + response.errorCode());
        }
    }

    private void startBot(long chatId, String userName) {
        SendMessage message = new SendMessage(chatId, "Hello, " + userName + "!");
        sendMessage(message);
    }

    private MessageState getState(String messageText) {
        if ("/start".equals(messageText))
            return MessageState.START;
        return MessageState.NOTIFY_TASK;
    }
}
