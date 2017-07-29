package nl.tudelft.jpacman.level;

import io.vavr.collection.List;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests various aspects of level.
 *
 * @author Jeroen Roosen 
 */
// The four suppress warnings ignore the same rule, which results in 4 same string literals
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyStaticImports"})
class LevelTest {

    /**
     * The level under test.
     */
    private Level level;

    /**
     * An NPC on this level.
     */
    private final Ghost ghost = mock(Ghost.class);

    /**
     * Starting position 1.
     */
    private final Square square1 = mock(Square.class);

    /**
     * Starting position 2.
     */
    private final Square square2 = mock(Square.class);

    /**
     * The board for this level.
     */
    private final Board board = mock(Board.class);

    private final Player player = mock(Player.class);
    /**
     * Sets up the level with the default board, a single NPC and a starting
     * square.
     */
    @BeforeEach
    void setUp() {
        final long defaultInterval = 100L;
        level = new Level(player, board, List.of(ghost), List.empty());
        when(ghost.getInterval()).thenReturn(defaultInterval);
    }

    /**
     * Validates the state of the level when it isn't started yet.
     */
    @Test
    void noStart() {
        assertThat(level.isInProgress()).isFalse();
    }

    /**
     * Validates the state of the level when it is stopped without starting.
     */
    @Test
    void stop() {
        level.stop();
        assertThat(level.isInProgress()).isFalse();
    }

    /**
     * Validates the state of the level when it is started.
     */
    @Test
    void start() {
        level.start();
        assertThat(level.isInProgress()).isTrue();
    }

    /**
     * Validates the state of the level when it is started then stopped.
     */
    @Test
    void startStop() {
        level.start();
        level.stop();
        assertThat(level.isInProgress()).isFalse();
    }
}
