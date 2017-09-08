package nl.tudelft.jpacman.level;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposables;
import io.vavr.Function1;
import io.vavr.collection.Vector;
import io.vavr.control.Either;
import io.vavr.control.Option;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.npc.ghost.Ghost;

import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Events {
    /**
     *
     * @param ghost the ghost to generate movement events for
     * @param index the index of the ghost in the entities vector
     * @return a flowable that will provide move timing events for the specified ghost
     * with the same behaviour as the NpcMoveTaks in jpacman 7.
     */
    public static Observable<Integer> ghostMovementEvents(Ghost ghost, int index) {
        return Observable.create(sub -> {
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, ghost.getClass().getSimpleName()));
            Runnable ghostTask = new Runnable() {
                @Override
                public void run() {
                    sub.onNext(index);
                    long interval = ghost.getInterval();
                    service.schedule(this, interval, TimeUnit.MILLISECONDS);
                }
            }; service.schedule(ghostTask,ghost.getInterval() / 2, TimeUnit.MILLISECONDS );

            sub.setDisposable(Disposables.fromAction(service::shutdown));
        });
    }

    public static Observable<Function1<Entities, Option<Entities>>> allEntityEvents(Observable<KeyEvent> playerEvents, Entities initial) {
        Observable<Function1<Entities, Option<Entities>>> playerMovements = playerEvents.compose(playerMovements());
        return Observable.merge(playerMovements, ghostMovements(initial.ghosts));
    }

    public static Observable<Function1<Entities, Option<Entities>>> ghostMovements(Vector<Ghost> ghosts) {
        return Observable.fromIterable(ghosts.zipWithIndex(Events::ghostMovementEvents))
            .flatMap(i -> i)
            .map(Events::ghostBliep);
    }

    public static ObservableTransformer<KeyEvent, Function1<Entities, Option<Entities>>> playerMovements() {
        return obs -> obs.map(Events::directionsFromKeyEvent)
            .map(d -> d.map(Events::playerMove).getOrElse(Option::of));
    }

    public static Function1<Entities, Option<Entities>> playerMove(Direction direction) {
        return e -> e.player.movePlayer(direction, e);
    }

    public static Option<Direction> directionsFromKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_UP:
                return Option.of(Direction.NORTH);
            case KeyEvent.VK_DOWN:
                return Option.of(Direction.SOUTH);
            case KeyEvent.VK_LEFT:
                return Option.of(Direction.WEST);
            case KeyEvent.VK_RIGHT:
                return Option.of(Direction.EAST);
            default:
                return Option.none();
        }
    }

    public static Function1<Entities, Option<Entities>> ghostBliep(int index) {
        return e -> e.ghosts.get(index).move(e);
    }
}
