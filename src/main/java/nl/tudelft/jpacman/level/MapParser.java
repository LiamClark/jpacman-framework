package nl.tudelft.jpacman.level;

import io.vavr.*;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import nl.tudelft.jpacman.PacmanConfigurationException;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import nl.tudelft.jpacman.sprite.PacManSprites;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

public interface MapParser<T> extends Function1<T, Try<Level>> {
    static MapParser<List<List<Character>>> characterMapParser(BoardFactory boardFactory, LevelFactory levelFactory) {
        return new MapParser<List<List<Character>>>() {
            @Override
            public Try<Level> apply(List<List<Character>> chars) {
                return Try.ofCallable(() -> {
                    List<List<Tuple4<Square, List<Ghost>, List<Pellet>, Option<Player>>>> squares = 
                        chars.zipWithIndex().map(row -> row._1.zipWithIndex().map(indexedChar -> squareForChar(boardFactory, levelFactory, row._2, indexedChar._2, indexedChar._1)));

                    Tuple3<List<Ghost>, List<Pellet>, Option<Player>> positions = squares.flatMap(Function.identity())
                        .map(a -> Tuple.of(a._2, a._3, a._4))
                        .fold(Tuple.of(List.empty(), List.empty(), Option.none()), (a, b) -> a.map(b._1::prependAll, b._2::prependAll, b._3::orElse));

                    Board board = boardFactory.createBoard(squares.map(xs -> xs.map(Tuple4::_1)).map(List::toArray).toArray());
                    Player player = positions._3.getOrElseThrow(() -> new PacmanConfigurationException("map must have a player"));

                    return levelFactory.createLevel(board, positions._1, positions._2, player);
                });
            }
        };
    }

    default Tuple4<Square, List<Ghost>, List<Pellet>, Option<Player>> squareForChar(BoardFactory boardCreator, LevelFactory levelCreator, int x, int y, char c) {
        switch (c) {
            case ' ':
                return justGround(boardCreator.createGround(x, y));
            case '#':
                return justGround(boardCreator.createWall(x, y));
            case '.':
                Square pelletSquare = boardCreator.createGround(x, y);
                Pellet pellet = levelCreator.createPellet(pelletSquare);
                return Tuple.of(pelletSquare, List.empty(), List.of(pellet), Option.none());
            case 'G':
                Square square = boardCreator.createGround(x, y);
                Ghost ghost = levelCreator.createGhost(square);
                return Tuple.of(square, List.of(ghost), List.empty(), Option.none());
            case 'P':
                Square ground = boardCreator.createGround(x, y);
                Player p = new PlayerFactory(new PacManSprites()).createPacMan(ground, Direction.EAST);
                return Tuple.of(ground, List.empty(), List.empty(), Option.of(p));
            default:
                throw new PacmanConfigurationException("Invalid character:" + c);
        }
    }


    static Tuple4<Square, List<Ghost>, List<Pellet>, Option<Player>> justGround(Square ground) {
        return Tuple.of(ground, List.empty(), List.empty(), Option.none());
    }

    static Function1<List<String>, Try<Level>> listMapParser(MapParser<List<List<Character>>> defaultParser) {
        Function<List<String>, List<List<Character>>> conversion = lines -> transpose(lines.map(String::toCharArray).map(List::ofAll));

        Function1<List<String>, Try<List<String>>> checkFormat = Function1.liftTry(Function1.of(MapParser::checkMapFormat));

        return checkFormat.andThen(strings -> strings.flatMap(defaultParser.compose(conversion)));
    }

    static <T> List<List<T>> transpose(List<List<T>> matrix) {
        if (matrix.exists(List::isEmpty)) {
            return List.empty();
        } else {
            List<List<T>> transpose = transpose(matrix.map(List::tail));
            List<T> map = matrix.map(List::head);
            return transpose.prepend(map);
        }
    }


    static Function1<String, Try<Level>> resourceMapParser(MapParser<List<List<Character>>> parser2) {
        Function1<String, Try<List<String>>> stringTryFunction1 = Function1.of(MapParser.class::getResource)
            .andThen(CheckedFunction1.liftTry(CheckedFunction1.of(URL::toURI)
                .andThen(Paths::get)
                .andThen(Files::readAllLines)
                .andThen(List::ofAll)
            ));

        return stringTryFunction1.andThen(chars -> chars.flatMap(listMapParser(parser2)));
    }

    /**
     * Check the correctness of the map lines in the text.
     *
     * @param text Map to be checked
     * @throws PacmanConfigurationException if map is not OK.
     */
    static List<String> checkMapFormat(List<String> text) {
        if (text == null) {
            throw new PacmanConfigurationException(
                "Input text cannot be null.");
        }

        if (text.isEmpty()) {
            throw new PacmanConfigurationException(
                "Input text must consist of at least 1 row.");
        }

        int width = text.get(0).length();

        if (width == 0) {
            throw new PacmanConfigurationException(
                "Input text lines cannot be empty.");
        }

        for (String line : text) {
            if (line.length() != width) {
                throw new PacmanConfigurationException(
                    "Input text lines are not of equal width.");
            }
        }

        return text;
    }
}
