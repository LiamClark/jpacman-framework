package nl.tudelft.jpacman.npc.ghost;

import io.vavr.Function1;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.level.Entities;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.LevelFactory;
import nl.tudelft.jpacman.level.MapParser;
import nl.tudelft.jpacman.level.Player;
import nl.tudelft.jpacman.sprite.PacManSprites;
import org.junit.jupiter.api.Test;

import static nl.tudelft.jpacman.board.Direction.*;
import static nl.tudelft.jpacman.board.Direction.WEST;
import static org.assertj.core.api.Assertions.assertThat;

class AStarTest {
    private final PacManSprites spriteStore = new PacManSprites();
    private Function1<List<String>, Try<Level>> parser = MapParser.listMapParser(
        MapParser.characterMapParser(new BoardFactory(spriteStore), new LevelFactory(spriteStore, new GhostFactory(spriteStore))));
    private Level level = parser.apply(List.of(
        "#########",
        "#   # #G#",
        "# #     #",
        "#P#G# # #",
        "#########"
    )).get();

    private final List<Direction> shortestPathWithTraveller = List.of(SOUTH, SOUTH, SOUTH, SOUTH, SOUTH, SOUTH, WEST, WEST, SOUTH, SOUTH, SOUTH, SOUTH, WEST);

    @Test
    void shortest_path_to_adjacent_square() {
        final Square destination = level.getBoard().squareAt(1, 2);
        final Player player = level.currentEntities().player;
        assertThat(AStar.astar(player.square, destination).get()).hasSize(1);
    }

    @Test
    void shortest_path_to_self() {
        final Player player = level.currentEntities().player;
        assertThat(AStar.astar(player.square, player.square).get()).hasSize(0);
    }

    @Test
    void shortest_path_to_bottom_ghost() {
        final Entities entities = level.currentEntities();
        final Ghost bottomGhost = entities.ghosts.get(0);
        assertThat(AStar.astar(entities.player.square, bottomGhost.square).get()).isEqualTo(List.of(EAST, EAST));
    }

    @Test
    void shortest_path_to_bottom_ghost_considering_terrain() {
        final Entities entities = level.currentEntities();
        final Ghost bottomGhost = entities.ghosts.get(0);
        assertThat(AStar.astarTraveller(entities.player.square, bottomGhost.square, entities.player).get())
            .isEqualTo(List.of(NORTH, NORTH, EAST, EAST, SOUTH, SOUTH));
    }


    @Test
    void new_path_finding_algorithm() {
        final Level level = new NavigationPerformanceTest.TestLauncher().getMapParser().apply("/ghostperformance.txt").get();
        final Entities entities = level.currentEntities();
        final Player player = entities.player;

        Square twoSquaresAheadOfPacman = Stream.continually(player.direction)
            .take(2)
            .foldRight(player.square, (direction, sq) -> sq.getSquareAt(direction));

        final Ghost blink = entities.ghosts.get(0);

        final Option<List<Direction>> astar = AStar.astarTraveller(blink.square, twoSquaresAheadOfPacman, blink);
        assertThat(astar.get()).hasSize(shortestPathWithTraveller.size());
        
    }
}
