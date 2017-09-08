package nl.tudelft.jpacman.npc.ghost;

import io.vavr.Tuple2;
import io.vavr.collection.Array;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;

import java.util.HashSet;
import java.util.function.Function;
import java.util.function.Predicate;


public class AStar {
    private static final Array<Direction> directions = Array.of(Direction.values());

    public static Option<List<Direction>> astar(Square from, Square to) {
        return astartInner(from, to, closed -> sq -> !closed.contains(sq));
    }

    public static Option<List<Direction>> astarTraveller(Square from, Square to, Unit traveller) {
        return astartInner(from, to, closed -> sq -> !closed.contains(sq) && sq.isAccessibleTo(traveller));
    }
    
    public static Option<List<Direction>> astartInner(Square from, Square to, Function<HashSet<Square>,Predicate<Square>> directionFilter) {
        final HashSet<Square> closed = new HashSet<>();
        final HashSet<Square> open = new HashSet<>();
        open.add(from);
        HashMap<Square, Square> cameFrom = HashMap.empty();

        Map<Square, Integer> gscores = gscores(from);
        Map<Square, Integer> fscores = fscores(from, to);
        while (!open.isEmpty()) {
            final Square current = cheapestOpenSquare(open, fscores);
            if(current.equals(to)) {
                return Option.of(reconstructPath(cameFrom, current));
            }

            open.remove(current);
            closed.add(current);

            final Array<Square> neighbours = directions.map(current::getSquareAt).filter(directionFilter.apply(closed));
            for (Square neighbour : neighbours) {
                open.add(neighbour);
                int tentativeGScore = gscores.getOrElse(current, Integer.MAX_VALUE) + 1;

                final Integer neighbourGscore = gscores.get(neighbour).getOrElse(Integer.MAX_VALUE);
                if(tentativeGScore < neighbourGscore) {
                    cameFrom = cameFrom.put(neighbour, current);
                    gscores = gscores.put(neighbour, tentativeGScore);
                    fscores = fscores.put(neighbour, tentativeGScore + neighbour.manhattanDistance(to));
                }
            }
        }

        return Option.none();
    }

    private static List<Direction> reconstructPath(HashMap<Square, Square> cameFrom, Square current) {
        final List<Square> squares = squaresInPath(cameFrom, current).prepend(current);
        return squares.sliding(2)
            .filter(xs -> xs.size() == 2)
            .map(sq -> sq.get(1).directionForNeighbour(sq.get(0))).toList()
            .flatMap(Option::toList)
            .reverse();
    }
    
    private static List<Square> squaresInPath(HashMap<Square, Square> cameFrom, Square current) {
        return cameFrom.get(current).map(sq -> squaresInPath(cameFrom, sq).prepend(sq)).getOrElse(List.empty());
    } 

    private static Square cheapestOpenSquare(HashSet<Square> open, Map<Square, Integer> fscores) {
        return fscores.filterKeys(open::contains).minBy(Tuple2::_2).map(Tuple2::_1).getOrElseThrow(() -> new IllegalStateException("there should always be a cheapest node in the open keys"));
    }

    private static Map<Square, Integer> gscores(Square from) {
        return HashMap.of(from, 0);
    }
    
    private static Map<Square, Integer> fscores(Square from, Square to) {
        return HashMap.of(from, from.manhattanDistance(to));
    }
}
