package nl.tudelft.jpacman.npc.ghost;

import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import nl.tudelft.jpacman.Launcher;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.level.Entities;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.level.LevelFactory;
import nl.tudelft.jpacman.level.Player;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static nl.tudelft.jpacman.board.Direction.SOUTH;
import static nl.tudelft.jpacman.board.Direction.WEST;
import static org.assertj.core.api.Assertions.assertThat;

public class NavigationPerformanceTest {
    private final List<Direction> shortestPathNoTraveller = List.of(SOUTH, SOUTH, SOUTH, SOUTH, SOUTH, SOUTH, SOUTH, SOUTH, SOUTH, SOUTH, WEST, WEST, WEST);
    private final List<Direction> shortestPathWithTraveller = List.of(SOUTH, SOUTH, SOUTH, SOUTH, SOUTH, SOUTH, WEST, WEST, SOUTH, SOUTH, SOUTH, SOUTH, WEST);

    @Test
    @Disabled
    void inky_performance_issue() {
        final Level level = new TestLauncher().getMapParser().apply("/ghostperformance.txt").get();
        final Entities entities = level.currentEntities();
        final Ghost inky = entities.ghosts.get(2);

        assertThat(inky).isInstanceOf(Inky.class);
        final long before = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            final Direction direction = inky.nextMove(entities);
        }
        final long after = System.currentTimeMillis();

        System.out.println(after - before);
    }

    @Test
    @Disabled
    void new_path_finding_algorithm() {
        final Level level = new TestLauncher().getMapParser().apply("/ghostperformance.txt").get();
        final Entities entities = level.currentEntities();
        final Player player = entities.player;

        Square twoSquaresAheadOfPacman = Stream.continually(player.direction)
            .take(2)
            .foldRight(player.square, (direction, sq) -> sq.getSquareAt(direction));

        final Ghost blink = entities.ghosts.get(0);
        for (int i = 0; i < 500; i++) {
            final Option<List<Direction>> astar = AStar.astarTraveller(blink.square, twoSquaresAheadOfPacman, blink);
            assertThat(astar.get()).hasSize(shortestPathWithTraveller.size());
        }
    }

    @Test
    @Disabled
    void old_path_finding_algorithm() {
        final Level level = new TestLauncher().getMapParser().apply("/ghostperformance.txt").get();
        final Entities entities = level.currentEntities();
        final Player player = entities.player;

        Square twoSquaresAheadOfPacman = Stream.continually(player.direction)
            .take(2)
            .foldRight(player.square, (direction, sq) -> sq.getSquareAt(direction));

        final Ghost blink = entities.ghosts.get(0);
        for (int i = 0; i < 500; i++) {
            final Option<List<Direction>> astar = Navigation.shortestPath(blink.square, twoSquaresAheadOfPacman, null);
            assertThat(astar).containsExactly(shortestPathNoTraveller);
        }

    }

    @Test
    @Disabled
    void visual_map() throws InterruptedException {
        new TestLauncher().withMapFile("/ghostperformance.txt").launch();
        Thread.sleep(100000);
    }


    public static class TestLauncher extends Launcher {
        @Override
        protected LevelFactory getLevelFactory() {
            return new LevelFactory(getSpriteStore(), getGhostFactory()) {
                private int ghostIndex = 0;
                private static final int GHOSTS = 4;
                private static final int BLINKY = 0;
                private static final int INKY = 1;
                private static final int PINKY = 2;
                private static final int CLYDE = 3;

                @Override
                public Ghost createGhost(Square square) {
                    ghostIndex++;
                    ghostIndex %= GHOSTS;
                    switch (ghostIndex) {
                        case BLINKY:
                            return ghostFact.createPinky(square, Direction.EAST);
                        case INKY:
                            return ghostFact.createBlinky(square, Direction.EAST);
                        case PINKY:
                            return ghostFact.createClyde(square, Direction.EAST);
                        case CLYDE:
                            return ghostFact.createInky(square, Direction.EAST);

                        //3
                        default:
                            return null;
                    }
                }
            };
        }
    }
}
