package nl.tudelft.jpacman.npc.ghost;

import io.vavr.collection.Array;
import io.vavr.control.Option;
import io.vavr.control.Try;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.level.Entities;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.sprite.Sprite;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An antagonist in the game of Pac-Man, a ghost.
 *
 * @author Jeroen Roosen
 */
public abstract class Ghost extends NPC {

    /**
     * The sprite map, one sprite for each direction.
     */
    protected final Map<Direction, Sprite> sprites;

    /**
     * The base movedTo interval of the ghost.
     */
    private final int moveInterval;

    /**
     * The random variation added to the {@link #moveInterval}.
     */
    private final int intervalVariation;

    /**
     * Creates a new ghost.
     *
     * @param spriteMap         The sprites for every direction.
     * @param moveInterval      The base interval of movement.
     * @param intervalVariation The variation of the interval.
     */
    protected Ghost(Square square, Direction direction, Map<Direction, Sprite> spriteMap, int moveInterval, int intervalVariation) {
        super(square, direction);
        this.sprites = spriteMap;
        this.intervalVariation = intervalVariation;
        this.moveInterval = moveInterval;
    }

    @Override
    public abstract Ghost movedTo(Square square, Direction direction);

    @Override
    public Sprite getSprite() {
        return sprites.get(direction);
    }

    public Option<Entities> move(Entities entities) {
        Direction direction = this.nextMove(entities);
        return this.targetLocation(direction)
            .map(sq -> entities.moveGhost(this, sq, direction));
    }

    @Override
    public long getInterval() {
        return this.moveInterval + ThreadLocalRandom.current().nextInt(this.intervalVariation);
    }

    /**
     * Determines a possible movedTo in a random direction.
     *
     * @return A direction in which the ghost can movedTo, or <code>null</code> if
     * the ghost is shut in by inaccessible squares.
     */
    protected Direction randomMove() {
        Array<Direction> directions = Array.of(Direction.values())
            .filter(d -> square.getSquareAt(d).isAccessibleTo(this));
        int i = ThreadLocalRandom.current().nextInt(directions.size());

        return Try.ofCallable(() -> directions.get(i)).getOrElse(Direction.EAST);
    }
}
