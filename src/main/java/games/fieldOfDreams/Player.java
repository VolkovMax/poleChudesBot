package games.fieldOfDreams;

import lombok.Getter;

/**
 * Класс описывающий игрока
 */
@Getter
public class Player {

    /**
     * Имя игрока
     */
    private final String name;

    /**
     * Очки игрока
     */
    private int score;

    /**
     * Конструктор
     *
     * @param name - имя игрока
     */
    public Player(String name) {
        this.name = name;
        this.score = 0;
    }

    /**
     * Присвоение очков игроку
     *
     * @param points - количество очков
     */
    public void addScore(int points) {
        this.score += points;
    }
}