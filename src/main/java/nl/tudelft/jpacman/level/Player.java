package nl.tudelft.jpacman.level;

import java.util.Map;

import io.vavr.control.Option;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.MovableUnit;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.sprite.AnimatedSprite;
import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A player operated unit in our game.
 *
 * @author Jeroen Roosen
 */
public class Player extends MovableUnit {

    /**
     * The amount of points accumulated by this player.
     */
    private int score;

    /**
     * The animations for every direction.
     */
    private final Map<Direction, Sprite> sprites;

    /**
     * The animation that is to be played when Pac-Man dies.
     */
    private final AnimatedSprite deathSprite;

    /**
     * <code>true</code> iff this player is alive.
     */
    private boolean alive;

    private Player(Square square, Direction direction, int score, Map<Direction, Sprite> sprites, AnimatedSprite deathSprite, boolean alive) {
        super(square, direction);
        this.score = score;
        this.sprites = sprites;
        this.deathSprite = deathSprite;
        this.alive = alive;
    }

    /**
     * Creates a new player with a score of 0 points.
     *
     * @param spriteMap
     *            A map containing a sprite for this player for every direction.
     * @param deathAnimation
     *            The sprite to be shown when this player dies.
     */
    protected Player(Square square, Direction direction, Map<Direction, Sprite> spriteMap, AnimatedSprite deathAnimation) {
        super(square, direction);
        this.score = 0;
        this.alive = true;
        this.sprites = spriteMap;
        this.deathSprite = deathAnimation;
        deathSprite.setAnimating(false);
    }

    @Override
    public Player movedTo(Square square, Direction direction) {
        return new Player(square, direction, sprites, deathSprite);
    }

    public Player die() {
        //todo apply death animation properly again.
        return new Player(square, direction, score, sprites, deathSprite, false);
    }

    public Player scorePoints(int points) {
        return new Player(square, direction, score + points, sprites, deathSprite, alive);
    }

    public Option<Entities> movePlayer(Direction direction, Entities entities) {
        return this.targetLocation(direction)
            .map(sq -> entities.movePlayer(sq, direction));
    }

    /**
     * Returns whether this player is alive or not.
     *
     * @return <code>true</code> iff the player is alive.
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Sets whether this player is alive or not.
     *
     * @param isAlive
     *            <code>true</code> iff this player is alive.
     */
    public void setAlive(boolean isAlive) {
        if (isAlive) {
            deathSprite.setAnimating(false);
        }
        if (!isAlive) {
            deathSprite.restart();
        }
        this.alive = isAlive;
    }

    /**
     * Returns the amount of points accumulated by this player.
     *
     * @return The amount of points accumulated by this player.
     */
    public int getScore() {
        return score;
    }

    @Override
    public Sprite getSprite() {
        if (isAlive()) {
            return sprites.get(direction);
        }
        return deathSprite;
    }

    /**
     * Adds points to the score of this player.
     *
     * @param points
     *            The amount of points to add to the points this player already
     *            has.
     */
    public void addPoints(int points) {
        score += points;
    }
}
