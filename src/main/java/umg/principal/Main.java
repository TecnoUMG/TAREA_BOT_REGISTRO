package umg.principal;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
public class Main {
    public static void main(String[] args) {
        try {
            TelegramBotsApi chatBot = new TelegramBotsApi(DefaultBotSession.class);
            chatBot.registerBot(new tareaBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}