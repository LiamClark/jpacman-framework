package nl.tudelft.jpacman.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test suite to confirm the public API of {@link Square} works as desired.
 *
 * @author Jeroen Roosen 
 */
class SquareTest {

    /**
     * The square under test.
     */
    private Square square;

    /**
     * Resets the square under test.
     */
    @BeforeEach
    void setUp() {
        square = new Ground(null);
    }
}
