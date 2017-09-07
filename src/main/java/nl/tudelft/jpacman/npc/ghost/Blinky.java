package nl.tudelft.jpacman.npc.ghost;


import io.vavr.collection.Stream;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.level.Entities;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.sprite.Sprite;

import java.util.Map;

/**
 * <p>
 * An implementation of the classic Pac-Man ghost Shadow.
 * </p>
 * <p>
 * Nickname: Blinky. As his name implies, Shadow is usually a constant shadow on
 * Pac-Man's tail. When he's not patrolling the top-right corner of the maze,
 * Shadow tries to find the quickest route to Pac-Man's position. Despite the
 * fact that Pinky's real name is Speedy, Shadow is actually the fastest of the
 * ghosts because of when there are only a few pellets left, Blinky drastically
 * speeds up, which can make him quite deadly. In the original Japanese version,
 * his name is Oikake/Akabei.
 * </p>
 * <p>
 * <b>AI:</b> When the ghosts are not patrolling in their home corners (Blinky:
 * top-right, Pinky: top-left, Inky: bottom-right, Clyde: bottom-left), Blinky
 * will attempt to shorten the distance between Pac-Man and himself. If he has
 * to choose between shortening the horizontal or vertical distance, he will
 * choose to shorten whichever is greatest. For example, if Pac-Man is four grid
 * spaces to the left, and seven grid spaces above Blinky, he'll try to movedTo up
 * towards Pac-Man before he moves to the left.
 * </p>
 * <p>
 * Source: http://strategywiki.org/wiki/Pac-Man/Getting_Started
 * </p>
 *
 * @author Jeroen Roosen
 */
public class Blinky extends Ghost {

    /**
     * The variation in intervals, this makes the ghosts look more dynamic and
     * less predictable.
     */
    private static final int INTERVAL_VARIATION = 50;

    /**
     * The base movement interval.
     */
    private static final int MOVE_INTERVAL = 250;

    /**
     * Creates a new "Blinky", a.k.a. "Shadow".
     *
     * @param spriteMap The sprites for this ghost.
     */
    // TODO Blinky should speed up when there are a few pellets left, but he
    // has no way to find out how many there are.
    public Blinky(Square square, Direction direction, Map<Direction, Sprite> spriteMap) {
        super(square, direction, spriteMap, MOVE_INTERVAL, INTERVAL_VARIATION);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>
     * When the ghosts are not patrolling in their home corners (Blinky:
     * top-right, Pinky: top-left, Inky: bottom-right, Clyde: bottom-left),
     * Blinky will attempt to shorten the distance between Pac-Man and himself.
     * If he has to choose between shortening the horizontal or vertical
     * distance, he will choose to shorten whichever is greatest. For example,
     * if Pac-Man is four grid spaces to the left, and seven grid spaces above
     * Blinky, he'll try to movedTo up towards Pac-Man before he moves to the left.
     * </p>
     */
    @Override
    public Direction nextMove(Entities entities) {
        // TODO Blinky should patrol his corner every once in a while
        // TODO Implement his actual behaviour instead of simply chasing.
        return Navigation.findNearest(Player.class, square, entities)
            .map(u -> u.square)
            //traveler was this
            .flatMap(target -> AStar.astar(square, target))
            .map(Stream::ofAll)
            .flatMap(Stream::headOption).getOrElse(randomMove());
    }

    @Override
    public Blinky movedTo(Square square, Direction direction) {
        return new Blinky(square, direction, this.sprites);
    }
}
