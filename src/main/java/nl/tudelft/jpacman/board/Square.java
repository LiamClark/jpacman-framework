package nl.tudelft.jpacman.board;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A square on a {@link Board}, which can (or cannot, depending on the type) be
 * occupied by units.
 *
 * @author Jeroen Roosen 
 */
public abstract class Square {

    /**
     * The collection of squares adjacent to this square.
     */
    private final Map<Direction, Square> neighbours;

    /**
     * Creates a new, empty square.
     */
    protected Square() {
        this.neighbours = new EnumMap<>(Direction.class);
    }

    /**
     * Returns the square adjacent to this square.
     *
     * @param direction
     *            The direction of the adjacent square.
     * @return The adjacent square in the given direction.
     */
    public Square getSquareAt(Direction direction) {
        return neighbours.get(direction);
    }

    /**
     * Links this square to a neighbour in the given direction. Note that this
     * is a one-way connection.
     *
     * @param neighbour
     *            The neighbour to link.
     * @param direction
     *            The direction the new neighbour is in, as seen from this cell.
     */
    public void link(Square neighbour, Direction direction) {
        neighbours.put(direction, neighbour);
    }

    /**
     * Determines whether the unit is allowed to occupy this square.
     *
     * @param unit
     *            The unit to grant or deny access.
     * @return <code>true</code> iff the unit is allowed to occupy this square.
     */
    public abstract boolean isAccessibleTo(Unit unit);

    /**
     * Returns the sprite of this square.
     *
     * @return The sprite of this square.
     */
    public abstract Sprite getSprite();

}
