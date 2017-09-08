package nl.tudelft.jpacman.board;

import nl.tudelft.jpacman.sprite.Sprite;

/**
 * A ground square is a square that is accessible to anyone.
 *
 * @author Jeroen Roosen
 */
public final class Ground extends Square {

    /**
     * The background for this square.
     */
    private final Sprite background;

    /**
     * Creates a new ground square.
     *
     * @param sprite
     *            The background for the square.
     */
    Ground(Sprite sprite, int x, int y) {
        super(x, y);
        this.background = sprite;
    }

    @Override
    public boolean isAccessibleTo(Unit unit) {
        return true;
    }

    @Override
    public Sprite getSprite() {
        return background;
    }
}
