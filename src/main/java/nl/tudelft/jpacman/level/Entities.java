package nl.tudelft.jpacman.level;

import com.google.common.collect.Iterables;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.collection.Stream;
import io.vavr.collection.Vector;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.board.Square;
import nl.tudelft.jpacman.board.Unit;
import nl.tudelft.jpacman.npc.ghost.Ghost;

import static java.util.function.Function.identity;

public class Entities {
    public final Map<Square, Pellet> pellets;
    public final Vector<Ghost> ghosts;
    public final Player player;

    public Entities(List<Pellet> pellets, Vector<Ghost> ghosts, Player player) {
        this.pellets = pellets.toMap(p -> p.square, identity());
        this.ghosts = ghosts;
        this.player = player;
    }

    private Entities(Map<Square, Pellet> pellets, Vector<Ghost> ghosts, Player player) {
        this.pellets = pellets;
        this.ghosts = ghosts;
        this.player = player;
    }

    public Vector<Unit> allUnits() {
        return Vector.<Unit>ofAll(ghosts).appendAll(pellets.values()).append(player);
    }

    public Entities moveGhost(Ghost ghost, Square square, Direction direction) {
        Vector<Ghost> movedGhosts = ghosts.replace(ghost, ghost.movedTo(square, direction));
        return new Entities(pellets, movedGhosts, ghostCollision(ghost, player));
    }

    public Entities movePlayer(Square square, Direction direction) {
        Player movedPlayer = player.movedTo(square, direction);
        return playerCollision(movedPlayer);
    }

    public Player ghostCollision(Ghost ghost, Player player) {
        if (player.occupies(ghost.square)) {
            return player.die();
        } else {
            return player;
        }
    }

    public Entities playerCollision(Player player) {
        Tuple2<Player, Map<Square, Pellet>> pelletCollidedPlayer = playerPelletCollision(player);
        Player ghostCollidedPlayer = playerGhostCollision(player, pelletCollidedPlayer._1);
        return new Entities(pelletCollidedPlayer._2, ghosts, ghostCollidedPlayer);
    }

    private Tuple2<Player, Map<Square, Pellet>> playerPelletCollision(Player player) {
        return this.pellets.get(player.square)
            .map(pellet -> Tuple.of(player.scorePoints(pellet.getValue()),
                pellets.remove(pellet.square)))
            .getOrElse(() -> Tuple.of(player, pellets));
    }

    private Player playerGhostCollision(Player player, Player scoredPlayer) {
        if (ghostAndPlayerOnSameSquare(player)) {
            return scoredPlayer.die();
        } else {
            return scoredPlayer;
        }
    }

    private boolean ghostAndPlayerOnSameSquare(Player player) {
        return ghosts.toStream()
            .map(u -> u.square)
            .exists(player.square::equals);
    }

    public int remainingPellets() {
        return pellets.size();
    }
}
