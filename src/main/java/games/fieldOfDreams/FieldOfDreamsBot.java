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

import static games.fieldOfDreams.Constants.BOT_NAME;
import static games.fieldOfDreams.Constants.BOT_TOKEN;
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

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            long userId = message.getFrom().getId();
            var userName = message.getFrom().getFirstName();
            var text = message.getText().trim().toLowerCase();

            if (text.equals("/games")) {
                startGame(chatId);
            } else if (text.matches("[а-яА-Я]")) { // Одна буква
                processGuess(chatId, userId, userName, text.charAt(0));
            } else if (gameSessions.containsKey(chatId) && text.length() == gameSessions.get(chatId).getWord().length()) { // Полное слово
                processWordGuess(chatId, userId, userName, text);
            }
        }
    }

    private void startGame(long chatId) {
        var word = getRandomWord();
        gameSessions.put(chatId, new GameSession(word));
        playerScores.put(chatId, new HashMap<>());
        sendMessage(chatId, "Новая игра началась! Загаданное слово: " + gameSessions.get(chatId).getHiddenWord());
    }

    private void processGuess(long chatId, long userId, String userName, char letter) {
        GameSession session = gameSessions.get(chatId);
        if (session == null) {
            sendMessage(chatId, "Начните игру с помощью команды /game.");
            return;
        }

        Player player = playerScores.get(chatId).computeIfAbsent(userId, id -> new Player(userName));

        if (session.isLetterUsed(letter)) {
            player.addScore(-1);
            sendMessage(chatId, userName + ", эта буква уже называлась! У вас -1 очко.");
            return;
        }

        boolean isCorrect = session.guessLetter(letter);
        String hiddenWord = session.getHiddenWord();

        if (isCorrect) {
            player.addScore(3);
            sendMessage(chatId, userName + ", верно! Буква '" + letter + "' есть в слове!\n" + hiddenWord);
        } else {
            sendMessage(chatId, userName + ", неверно! Буквы '" + letter + "' нет в слове.\n" + hiddenWord);
        }

        if (session.isWordGuessed()) {
            endGame(chatId, userId, userName, session.getWord(), true);
        }
    }

    private void processWordGuess(long chatId, long userId, String userName, String word) {
        GameSession session = gameSessions.get(chatId);
        if (session == null) return;

        Player player = playerScores.get(chatId).computeIfAbsent(userId, id -> new Player(userName));

        if (word.equals(session.getWord())) {
            player.addScore(session.getWord().length() * 2);
            endGame(chatId, userId, userName, word, true);
        } else {
            sendMessage(chatId, userName + ", неправильное слово! Попробуйте ещё раз.");
        }
    }

    private void endGame(long chatId, long userId, String userName, String word, boolean guessed) {
        String message = guessed ?
                "Поздравляем, " + userName + "! Вы угадали слово: " + word :
                "Игра окончена! Загаданное слово было: " + word;

        message += "\nОчки участников:\n" + formatScores(chatId);
        sendMessage(chatId, message);
        gameSessions.remove(chatId);
        playerScores.remove(chatId);
    }

    private String formatScores(long chatId) {
        StringBuilder sb = new StringBuilder();
        playerScores.get(chatId).values().forEach(player ->
                sb.append(player.getName()).append(": ").append(player.getScore()).append(" очков\n"));
        return sb.toString();
    }

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

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}