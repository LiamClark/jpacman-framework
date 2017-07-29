package nl.tudelft.jpacman.level;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import nl.tudelft.jpacman.board.*;
import nl.tudelft.jpacman.npc.ghost.Ghost;

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
    private final Vector<Ghost> ghosts;
    private final Vector<Pellet> pellets;

    /**
     * The lock that ensures moves are executed sequential.
     */
    private final Object moveLock = new Object();

    /**
     * The lock that ensures starting and stopping can't interfere with each
     * other.
     */
    private final Object startStopLock = new Object();

    /**
     * The NPCs of this level and, if they are running, their schedules.
     */
    private final Map<Ghost, ScheduledExecutorService> npcs;

    /**
     * <code>true</code> iff this level is currently in progress, i.e. players
     * and NPCs can movedTo.
     */
    private boolean inProgress;

    /**
     * The players on this level.
     */
    public Player player;

    /**
     * The table of possible collisions between units.
     */
    private final CollisionMap collisions;

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
     * @param collisionMap
     *            The collection of collisions that should be handled.
     */
    public Level(Player player, Board board, List<Ghost> ghosts, List<Pellet> pellets,
                 CollisionMap collisionMap) {
        this.board = board;
        this.ghosts = ghosts.toVector();
        this.pellets = pellets.toVector();
        this.inProgress = false;
        this.npcs = ghosts.toJavaMap(g -> Tuple.of(g, null));
        this.player = player;
        this.collisions = collisionMap;
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

    /**
     * Moves the unit into the given direction if possible and handles all
     * collisions.
     *
     * @param unit
     *            The unit to movedTo.
     * @param direction
     *            The direction to movedTo the unit in.
     */
    private <T extends MovableUnit> void move(T unit, Direction direction, Predicate<Square> collisionCondition) {
        assert unit != null;

        if (!isInProgress()) {
            return;
        }

        synchronized (moveLock) {
            Square location = unit.square;
            Square destination = location.getSquareAt(direction);
            MovableUnit movedUnit = unit.movedTo(destination, direction);

            if (destination.isAccessibleTo(unit)) {
                java.util.List<Unit> occupants = destination.getOccupants();
                if(collisionCondition.test(destination)) {
                    for (Unit occupant : occupants) {
                        collisions.collide(unit, occupant);
                    }
                }
            }
            updateObservers();
        }
    }
    
    public void move(Ghost ghost, Direction direction) {
        move(ghost, direction, destination -> player.square.equals(destination));
    }

    public void move(Player player, Direction direction) {
        move(player, direction, a -> true);
    }

    /**
     * Starts or resumes this level, allowing movement and (re)starting the
     * NPCs.
     */
    public void start() {
        synchronized (startStopLock) {
            if (isInProgress()) {
                return;
            }
            startNPCs();
            inProgress = true;
            updateObservers();
        }
    }

    /**
     * Stops or pauses this level, no longer allowing any movement on the board
     * and stopping all NPCs.
     */
    public void stop() {
        synchronized (startStopLock) {
            if (!isInProgress()) {
                return;
            }
            stopNPCs();
            inProgress = false;
        }
    }

    /**
     * Starts all NPC movement scheduling.
     */
    private void startNPCs() {
        for (final Ghost npc : Stream.ofAll(npcs.keySet())) {
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

            service.schedule(new NpcMoveTask(service, npc),
                npc.getInterval() / 2, TimeUnit.MILLISECONDS);

            npcs.put(npc, service);
        }
    }

    /**
     * Stops all NPC movement scheduling and interrupts any movements being
     * executed.
     */
    private void stopNPCs() {
        for (Entry<Ghost, ScheduledExecutorService> entry : npcs.entrySet()) {
            ScheduledExecutorService schedule = entry.getValue();
            assert schedule != null;
            schedule.shutdownNow();
        }
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

    /**
     * Returns <code>true</code> iff at least one of the players in this level
     * is alive.
     *
     * @return <code>true</code> if at least one of the registered players is
     *         alive.
     */
    public boolean isAnyPlayerAlive() {
        return player.isAlive();
    }

    /**
     * Counts the pellets remaining on the board.
     *
     * @return The amount of pellets remaining on the board.
     */
    public int remainingPellets() {
        return getBoard().remainingPellets();
    }

    public Vector<Unit> getAllUnits() {
        return Vector.<Unit>empty().appendAll(ghosts).appendAll(pellets).append(player);
    }

    /**
     * A task that moves an NPC and reschedules itself after it finished.
     *
     * @author Jeroen Roosen
     */
    private final class NpcMoveTask implements Runnable {

        /**
         * The service executing the task.
         */
        private final ScheduledExecutorService service;

        /**
         * The NPC to movedTo.
         */
        private final Ghost npc;

        /**
         * Creates a new task.
         *
         * @param service
         *            The service that executes the task.
         * @param npc
         *            The NPC to movedTo.
         */
        NpcMoveTask(ScheduledExecutorService service, Ghost npc) {
            this.service = service;
            this.npc = npc;
        }

        @Override
        public void run() {
            Direction nextMove = npc.nextMove();
            if (nextMove != null) {
                move(npc, nextMove);
            }
            long interval = npc.getInterval();
            service.schedule(this, interval, TimeUnit.MILLISECONDS);
        }
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
