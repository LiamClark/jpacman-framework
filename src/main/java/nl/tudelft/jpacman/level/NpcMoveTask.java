package nl.tudelft.jpacman.level;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.npc.ghost.Ghost;

/**
 * A task that moves an NPC and reschedules itself after it finished.
 *
 * @author Jeroen Roosen
 */
final class NpcMoveTask implements Runnable {

    private Level level;
    /**
     * The service executing the task.
     */
    private final ScheduledExecutorService service;

    private final int ghostIndex;

    /**
     * Creates a new task.
     *
     * @param service
     *            The service that executes the task.
     * @param npc
     *            The NPC to movedTo.
     */
    NpcMoveTask(Level level, ScheduledExecutorService service, Ghost npc) {
        this.level = level;
        this.service = service;
        ghostIndex = level.getEntities().ghosts.indexOf(npc);
    }

    @Override
    public void run() {
        Ghost ghost = level.getEntities().ghosts.get(ghostIndex);
        Direction nextMove = ghost.nextMove();
        if (nextMove != null) {
            level.move(ghost, nextMove);
        }
        long interval = ghost.getInterval();
        service.schedule(this, interval, TimeUnit.MILLISECONDS);
    }
}
