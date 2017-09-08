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


//    /**
//     * Verifies that the nearest object is detected.
//     */
//    @Test
//    void testNearestUnit() {
//        Board b = parser
//            .apply(List.of("#####", "# ..#", "#####"))
//            .get()
//            .getBoard();
//        Square s1 = b.squareAt(1, 1);
//        Square s2 = b.squareAt(2, 1);
//        Option<Square> result = Navigation.findNearest(Pellet.class, s1, entities).map(u -> u.square);
//        assertThat(result.get()).isEqualTo(s2);
//    }
//
//    /**
//     * Verifies that there is no such location if there is no nearest object.
//     */
//    @Test
//    void testNoNearestUnit() {
//        Board b = parser.apply(List.of(" ")).get().getBoard();
//        Square s1 = b.squareAt(0, 0);
//        Option<Pellet> unit = Navigation.findNearest(Pellet.class, s1, entities);
//        assertThat(unit).isEmpty();
//    }
//
//    /**
//     * Verifies that there is ghost on the default board
//     * next to cell [1, 1].
//     */
//    @Test
//    void testFullSizedLevel() {
//        Board b = MapParser.resourceMapParser(defaultParser).apply("/board.txt").get().getBoard();
//        Square s1 = b.squareAt(1, 1);
//        Option<Ghost> unit = Navigation.findNearest(Ghost.class, s1, entities);
//        assertThat(unit).isNotNull();
//    }
}
