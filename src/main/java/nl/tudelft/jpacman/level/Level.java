package nl.tudelft.jpacman.level;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.npc.ghost.Ghost;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A level of Pac-Man. A level consists of the board with the players and the
 * AIs on it.
 *
 * @author Jeroen Roosen 
 */
@SuppressWarnings("PMD.TooManyMethods")
public class Level {

    /**
     * The board of this level.
     */
    private final Board board;
    private final AtomicReference<Entities> entities;

    /*** <code>true</code> iff this level is currently in progress, i.e. players
     * and NPCs can movedTo.
     */
    private boolean inProgress;
    /**
     * The objects observing this level.
     */
    private final Set<LevelObserver> observers;

    /**
     * Creates a new level for the board.
     *
     * @param board
     *            The board for the level.
     * @param ghosts
     *            The ghosts on the board.
     */
    public Level(Player player, Board board, List<Ghost> ghosts, List<Pellet> pellets) {
        this.board = board;
        this.inProgress = false;
        this.entities = new AtomicReference<>(new Entities(pellets, ghosts.toVector(), player));
        this.observers = new HashSet<>();
    }

    /**
     * Adds an observer that will be notified when the level is won or lost.
     *
     * @param observer
     *            The observer that will be notified.
     */
    public void addObserver(LevelObserver observer) {
        observers.add(observer);
    }

    /**
     * Removes an observer if it was listed.
     *
     * @param observer
     *            The observer to be removed.
     */
    public void removeObserver(LevelObserver observer) {
        observers.remove(observer);
    }


    /**
     * Returns the board of this level.
     *
     * @return The board of this level.
     */
    public Board getBoard() {
        return board;
    }

    // no entity operation should be allowed when the level is not in progress
    public Entities entityOperation(Entities entities, Function1<Entities, Option<Entities>> entityOperation) {
        if(this.isInProgress()) {
            return entityOperation.apply(entities).getOrElse(entities);
        } else {
            return entities;
        }
    }

    /**
     * Starts or resumes this level, allowing movement and (re)starting the
     * NPCs.
     */
    public void start() {
            if (isInProgress()) {
                return;
            }
            inProgress = true;
            updateObservers();

    }

    /**
     * Stops or pauses this level, no longer allowing any movement on the board
     * and stopping all NPCs.
     */
    public void stop() {
            if (!isInProgress()) {
                return;
            }
            inProgress = false;
    }


    /**
     * Returns whether this level is in progress, i.e. whether moves can be made
     * on the board.
     *
     * @return <code>true</code> iff this level is in progress.
     */
    public boolean isInProgress() {
        return inProgress;
    }

    /**
     * Updates the observers about the state of this level.
     */
    private void updateObservers() {
        if (!isAnyPlayerAlive()) {
            for (LevelObserver observer : observers) {
                observer.levelLost();
            }
        }
        if (remainingPellets() == 0) {
            for (LevelObserver observer : observers) {
                observer.levelWon();
            }
        }
    }

    public Entities currentEntities() {
        return entities.get();
    }

    public void setCurrentEntities(Entities entities) {
        this.entities.set(entities);
    }

    /**
     * Returns <code>true</code> iff at least one of the players in this level
     * is alive.
     *
     * @return <code>true</code> if at least one of the registered players is
     *         alive.
     */
    public boolean isAnyPlayerAlive() {
        return entities.get().player.isAlive();
    }

    public Player getPlayer() {
        return entities.get().player;
    }

    /**
     * Counts the pellets remaining on the board.
     *
     * @return The amount of pellets remaining on the board.
     */
    public int remainingPellets() {
        return entities.get().remainingPellets();
    }

    public Vector<Unit> getAllUnits() {
        return entities.get().allUnits();
    }

    /**
     * An observer that will be notified when the level is won or lost.
     *
     * @author Jeroen Roosen
     */
    public interface LevelObserver {

        /**
         * The level has been won. Typically the level should be stopped when
         * this event is received.
         */
        void levelWon();

        /**
         * The level has been lost. Typically the level should be stopped when
         * this event is received.
         */
        void levelLost();
    }
}
