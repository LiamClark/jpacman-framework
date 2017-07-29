package nl.tudelft.jpacman.board;

import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A unit that can be placed on a {@link Square}.
 *
 * @author Jeroen Roosen 
 */
public abstract class Unit {

    /**
     * The square this unit is currently occupying.
     */
    public final Square square;

    protected Unit(Square square) {
        this.square = square;
    }

    public boolean occupies(Square square) {
        return this.square.equals(square);
    }
    /**
     * Returns the sprite of this unit.
     *
     * @return The sprite of this unit.
     */
    public abstract Sprite getSprite();

}
