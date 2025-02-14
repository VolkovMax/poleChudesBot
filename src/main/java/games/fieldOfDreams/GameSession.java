package games.fieldOfDreams;

import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * Класс игровой сессии
 */
@Getter
public class GameSession {

    /**
     * Загаданное слово
     */
    private final String word;

    /**
     * Загаданное слово, где скрыты буквы, которые не отгаданы
     */
    private final StringBuilder hiddenWord;

    /**
     * Список названных букв для загаданного слова
     */
    private final Set<Character> usedLetters = new HashSet<>();

    /**
     * Конструктор игровой сессии
     *
     * @param word - загаданное слово
     */
    public GameSession(String word) {
        this.word = word;
        this.hiddenWord = new StringBuilder(word.replaceAll(".", "❏"));
    }

    /**
     * Метод определяющий есть ли буква в слове
     *
     * @param letter - буква, которую назвал пользователь
     * @return есть ли данная буква в слове
     */
    public boolean guessLetter(char letter) {
        usedLetters.add(letter);
        boolean isCorrect = false;
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) == letter) {
                hiddenWord.setCharAt(i, letter);
                isCorrect = true;
            }
        }
        return isCorrect;
    }

    /**
     * Определяет, называлась ли данная буква в текущей сессии
     * @param letter - буква названная пользователем
     */
    public boolean isLetterUsed(char letter) {
        return usedLetters.contains(letter);
    }

    /**
     * Получить слово скрывающее неугаданные буквы
     */
    public String getHiddenWord() {
        return hiddenWord.toString();
    }

    /**
     * Определяет отгадано ли слово
     */
    public boolean isWordGuessed() {
        return hiddenWord.toString().equals(word);
    }
}