package nl.tudelft.jpacman.level;

import io.vavr.*;
import io.vavr.collection.List;
import io.vavr.control.Try;
import nl.tudelft.jpacman.PacmanConfigurationException;
import nl.tudelft.jpacman.board.Board;
import nl.tudelft.jpacman.board.BoardFactory;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.npc.NPC;

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
                    Function1<Character, Tuple3<Square, List<NPC>, List<Square>>> squareForChar = Function3.of(this::squareForCharWriter).apply(boardFactory).apply(levelFactory);
                    List<List<Tuple3<Square, List<NPC>, List<Square>>>> squares = chars.map(row -> row.map(squareForChar));

                    Tuple2<List<NPC>, List<Square>> positions = squares.flatMap(Function.identity())
                        .map(a -> Tuple.of(a._2, a._3))
                        .fold(Tuple.of(List.empty(), List.empty()), (a, b) -> a.map(b._1::prependAll, b._2::prependAll));

                    Board board = boardFactory.createBoard(squares.map(xs -> xs.map(Tuple3::_1)).map(List::toArray).toArray());
                    return levelFactory.createLevel(board, positions._1, positions._2);
                });
            }
        };
    }

    default Tuple3<Square, List<NPC>, List<Square>> squareForCharWriter(BoardFactory boardCreator, LevelFactory levelCreator, char c) {
        switch (c) {
            case ' ':
                return justGround(boardCreator.createGround());
            case '#':
                return justGround(boardCreator.createWall());
            case '.':
                Square pelletSquare = boardCreator.createGround();
                levelCreator.createPellet().occupy(pelletSquare);
                return justGround(pelletSquare);
            case 'G':
                NPC ghost = levelCreator.createGhost();
                Square square = boardCreator.createGround();
                ghost.occupy(square);
                return Tuple.of(square, List.of(ghost), List.empty());
            case 'P':
                Square ground = boardCreator.createGround();
                return Tuple.of(ground, List.empty(), List.of(ground));
            default:
                throw new PacmanConfigurationException("Invalid character:" + c);
        }
    }

    static Tuple3<Square, List<NPC>, List<Square>> justGround(Square ground) {
        return new Tuple3<>(ground, List.empty(), List.empty());
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
