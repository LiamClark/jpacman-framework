package nl.tudelft.jpacman.npc.ghost;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.NPC;
import nl.tudelft.jpacman.sprite.Sprite;

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
     * @param spriteMap
     *            The sprites for every direction.
     * @param moveInterval
     *            The base interval of movement.
     * @param intervalVariation
     *            The variation of the interval.
     */
    protected Ghost(Square square , Direction direction, Map<Direction, Sprite> spriteMap, int moveInterval, int intervalVariation) {
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

    @Override
    public long getInterval() {
        return this.moveInterval + new Random().nextInt(this.intervalVariation);
    }

    /**
     * Determines a possible movedTo in a random direction.
     *
     * @return A direction in which the ghost can movedTo, or <code>null</code> if
     *         the ghost is shut in by inaccessible squares.
     */
     protected Direction randomMove() {
        List<Direction> directions = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (square.getSquareAt(direction).isAccessibleTo(this)) {
                directions.add(direction);
            }
        }
        if (directions.isEmpty()) {
            return null;
        }
        int i = new Random().nextInt(directions.size());
        return directions.get(i);
    }
}
