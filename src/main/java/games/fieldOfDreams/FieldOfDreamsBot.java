package games.fieldOfDreams;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

import static games.fieldOfDreams.Constants.*;
import static games.fieldOfDreams.Words.getRandomTrackPhrase;
import static games.fieldOfDreams.Words.getRandomWord;

/**
 * Класс игры Поле чудес
 */
@Getter
@Setter
public class FieldOfDreamsBot extends TelegramLongPollingBot {

    /**
     * Данные игровой сессии
     */
    private Map<Long, GameSession> gameSessions = new HashMap<>();

    /**
     * Данные с заработанными очками
     */
    private Map<Long, Map<Long, Player>> playerScores = new HashMap<>();

    /**
     * Переопределенный метод обработки сообщения от пользователя
     *
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            var message = update.getMessage();
            long chatId = message.getChatId();
            long userId = message.getFrom().getId();
            var userName = message.getFrom().getFirstName();
            var text = message.getText().trim().toLowerCase();

            //запускаем игру Поле чудес
            if (text.equals("/startfield")) {
                startGame(chatId);
            }
            //проверяем букву, есть ли она в слове
            else if (text.matches("[а-яА-Я]")) {
                processGuess(chatId, userId, userName, text.charAt(0));
            }
            //проверяем названное пользователем слово
            else if (gameSessions.containsKey(chatId)
                    && text.length() == gameSessions.get(chatId).getWord().length()) {
                processWordGuess(chatId, userId, userName, text);
            }
            else if (text.equals("/track"))
            {
                sendMessage(chatId, getRandomTrackPhrase());
            }
        }
    }

    /**
     * Начало игры
     * @param chatId - id чата, в котором запущена игра
     */
    private void startGame(long chatId) {
        var word = getRandomWord();
        gameSessions.put(chatId, new GameSession(word));
        playerScores.put(chatId, new HashMap<>());
        sendMessage(chatId, "Я сказала СТАРТУЕМ! (С) Загаданное слово: " + gameSessions.get(chatId).getHiddenWord());
    }

    /**
     * Метод обрабатывающий введенную пользователем букву
     * @param chatId - id чата, в котором запущена игра
     * @param userId - id пользователя
     * @param userName - имя пользователя
     * @param letter - введенная буква
     */
    private void processGuess(long chatId, long userId, String userName, char letter) {
        GameSession session = gameSessions.get(chatId);
        if (session == null) {
            sendMessage(chatId, "Начни игру с помощью команды /startfield.");
            return;
        }

        Player player = playerScores.get(chatId).computeIfAbsent(userId, id -> new Player(userName));

        if (session.isLetterUsed(letter)) {
            player.addScore(-1);
            sendMessage(chatId, userName + ", не, ну ты индеец, я балдю. Была уже такая буква. У тебя -1 очко.");
            return;
        }

        boolean isCorrect = session.guessLetter(letter);
        String hiddenWord = session.getHiddenWord();

        if (isCorrect) {
            player.addScore(2);
            sendMessage(chatId, userName + ", ура, ёпта! Буква '" + letter + "' есть в слове!\n" + hiddenWord);
        } else {
            sendMessage(chatId, userName + ", ты ебобо? Буквы '" + letter + "' нет в слове.\n" + hiddenWord);
        }

        if (session.isWordGuessed()) {
            endGame(chatId, userId, userName, session.getWord(), true);
        }
    }

    /**
     * Метод обрабатывающий введенное пользователем слово
     * @param chatId - id чата, в котором запущена игра
     * @param userId - id пользователя
     * @param userName - имя пользователя
     * @param word - введенное пользователем слово
     */
    private void processWordGuess(long chatId, long userId, String userName, String word) {
        GameSession session = gameSessions.get(chatId);
        if (session == null) return;

        Player player = playerScores.get(chatId).computeIfAbsent(userId, id -> new Player(userName));

        if (word.equals(session.getWord())) {
            player.addScore(session.getWord().length() * 2);
            endGame(chatId, userId, userName, word, true);
        } else {
            sendMessage(chatId, userName + ", давай по новой, Миша, все хуйня.");
        }
    }

    /**
     * Метод обрабатывающий окончание игры
     * @param chatId - id чата, в котором запущена игра
     * @param userName - имя пользователя
     * @param word - введенное пользователем слово
     * @param guessed - отгадано ли слово
     */
    private void endGame(long chatId, long userId, String userName, String word, boolean guessed) {
        String message = guessed ?
                "Йуху, " + userName + "! Наконец-то ты угадал слово: " + word :
                "Игра окончена!";

        message += "\nОчки участников:\n" + formatScores(chatId);
        sendMessage(chatId, message);
        gameSessions.remove(chatId);
        playerScores.remove(chatId);
    }

    /**
     * Метод вывода очков участников
     * @param chatId - id чата, в котором запущена игра
     */
    private String formatScores(long chatId) {
        StringBuilder sb = new StringBuilder();
        playerScores.get(chatId).values().forEach(player ->
                sb.append(player.getName()).append(": ").append(player.getScore()).append(" очков\n"));
        return sb.toString();
    }

    /**
     * Метод отправки сообщений ботом
     * @param chatId - id чата, в котором запущена игра
     * @param text - текст сообщения
     */
    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Получить имя бота
     */
    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    /**
     * Получить токен бота
     */
    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}