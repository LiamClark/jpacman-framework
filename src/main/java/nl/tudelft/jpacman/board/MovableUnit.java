package nl.tudelft.jpacman.board;

public abstract class MovableUnit extends Unit {
    public final Direction direction;

    protected MovableUnit(Square square, Direction direction) {
        super(square);
        this.direction = direction;
    }

    public abstract MovableUnit movedTo(Square square, Direction direction);
}
