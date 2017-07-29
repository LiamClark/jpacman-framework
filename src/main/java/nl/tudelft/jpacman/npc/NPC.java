package nl.tudelft.jpacman.npc;

import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.MovableUnit;
import nl.tudelft.jpacman.board.Square;

/**
 * A non-player unit.
 *
 * @author Jeroen Roosen 
 */
public abstract class NPC extends MovableUnit {

    protected NPC(Square square, Direction direction) {
        super(square, direction);
    }

    /**
     * The time that should be taken between moves.
     *
     * @return The suggested delay between moves in milliseconds.
     */
    public abstract long getInterval();

    /**
     * Calculates the next movedTo for this unit and returns the direction to movedTo
     * in.
     *
     * Precondition: The NPC occupies a square (hasSquare() holds).
     *
     * @return The direction to movedTo in, or <code>null</code> if no movedTo could
     *         be devised.
     */
    public abstract Direction nextMove();

}
