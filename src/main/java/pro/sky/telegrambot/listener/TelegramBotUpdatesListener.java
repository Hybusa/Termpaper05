package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;

    public TelegramBotUpdatesListener(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            switch (update.message().text()) {
                case "/start":
                    startBot(update.message().chat().id(), update.message().chat().firstName());
                    break;
                default:
                    logger.info("Unexpected");
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void startBot(long chatId, String userName) {
        SendMessage message = new SendMessage(chatId, "Hello, " + userName + "!");

            SendResponse response = telegramBot.execute(message);
            if(response.isOk()){
                logger.info("Reply sent");
            }else{
                logger.error("Error sending. Code: " + response.errorCode());
            }
    }
}
