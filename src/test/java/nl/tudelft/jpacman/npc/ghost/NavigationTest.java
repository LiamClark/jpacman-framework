package nl.tudelft.jpacman.npc.ghost;

import static org.mockito.Mockito.mock;

import static org.assertj.core.api.Assertions.assertThat;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.LevelFactory;
import nl.tudelft.jpacman.level.MapParser;
import nl.tudelft.jpacman.level.Pellet;
import nl.tudelft.jpacman.sprite.PacManSprites;

import org.junit.jupiter.api.Test;

/**
 * Tests the various methods provided by the {@link Navigation} class.
 *
 * @author Jeroen Roosen
 */
@SuppressWarnings({"magicnumber", "PMD.AvoidDuplicateLiterals"})
class NavigationTest {

    /**
     * Map parser used to construct boards.
     */
    private PacManSprites sprites = new PacManSprites();
    private MapParser<List<List<Character>>> defaultParser = MapParser.characterMapParser(new BoardFactory(sprites),
        new LevelFactory(sprites, new GhostFactory(sprites)));
    private Function1<List<String>, Try<Level>> parser = MapParser.listMapParser(defaultParser);

    /**
     * Verifies that the path to the same square is empty.
     */
    @Test
    void testShortestPathEmpty() {
        Board b = parser.apply(List.of(" ")).get().getBoard();
        Square s1 = b.squareAt(0, 0);
        Square s2 = b.squareAt(0, 0);
        Option<List<Direction>> path = Navigation
            .shortestPath(s1, s2, mock(Unit.class));
        assertThat(path.get()).isEmpty();
    }

    /**
     * Verifies that if no path exists, the result is <code>null</code>.
     */
    @Test
    void testNoShortestPath() {
        Board b = parser
            .apply(List.of("#####", "# # #", "#####"))
            .get()
            .getBoard();
        Square s1 = b.squareAt(1, 1);
        Square s2 = b.squareAt(3, 1);
        Option<List<Direction>> path = Navigation
            .shortestPath(s1, s2, mock(Unit.class));
        assertThat(path).isEmpty();
    }

    /**
     * Verifies that having no traveller ignores terrain.
     */
    @Test
    void testNoTraveller() {
        Board b = parser
            .apply(List.of("#####", "# # #", "#####"))
            .get()
            .getBoard();
        Square s1 = b.squareAt(1, 1);
        Square s2 = b.squareAt(3, 1);
        Option<List<Direction>> path = Navigation.shortestPath(s1, s2, null);
        assertThat(path.get()).containsExactly(Direction.EAST, Direction.EAST);
    }

    /**
     * Tests if the algorithm can find a path in a straight line.
     */
    @Test
    void testSimplePath() {
        Board b = parser.apply(List.of("####", "#  #", "####"))
            .get()
            .getBoard();
        Square s1 = b.squareAt(1, 1);
        Square s2 = b.squareAt(2, 1);
        Option<List<Direction>> path = Navigation
            .shortestPath(s1, s2, mock(Unit.class));
        assertThat(path.get()).containsExactly(Direction.EAST);
    }

    /**
     * Verifies that the algorithm can find a path when it has to take corners.
     */
    @Test
    void testCornerPath() {
        Board b = parser.apply(
            List.of("####", "#  #", "## #", "####")).get().getBoard();
        Square s1 = b.squareAt(1, 1);
        Square s2 = b.squareAt(2, 2);
        Option<List<Direction>> path = Navigation
            .shortestPath(s1, s2, mock(Unit.class));
        assertThat(path.get()).containsExactly(Direction.EAST, Direction.SOUTH);
    }

    /**
     * Verifies that the nearest object is detected.
     */
    @Test
    void testNearestUnit() {
        Board b = parser
            .apply(List.of("#####", "# ..#", "#####"))
            .get()
            .getBoard();
        Square s1 = b.squareAt(1, 1);
        Square s2 = b.squareAt(2, 1);
        Option<Square> result = Navigation.findNearest(Pellet.class, s1).map(u -> u.square);
        assertThat(result.get()).isEqualTo(s2);
    }

    /**
     * Verifies that there is no such location if there is no nearest object.
     */
    @Test
    void testNoNearestUnit() {
        Board b = parser.apply(List.of(" ")).get().getBoard();
        Square s1 = b.squareAt(0, 0);
        Option<Pellet> unit = Navigation.findNearest(Pellet.class, s1);
        assertThat(unit).isEmpty();
    }

    /**
     * Verifies that there is ghost on the default board
     * next to cell [1, 1].
     */
    @Test
    void testFullSizedLevel() {
        Board b = MapParser.resourceMapParser(defaultParser).apply("/board.txt").get().getBoard();
        Square s1 = b.squareAt(1, 1);
        Option<Ghost> unit = Navigation.findNearest(Ghost.class, s1);
        assertThat(unit).isNotNull();
    }
}
