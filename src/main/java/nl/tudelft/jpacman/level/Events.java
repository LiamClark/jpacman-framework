package nl.tudelft.jpacman.level;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import io.vavr.Function1;
import io.vavr.collection.Vector;
import io.vavr.control.Option;
import nl.tudelft.jpacman.board.Direction;
import nl.tudelft.jpacman.npc.ghost.Ghost;

import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;


public class Events {
    /**
     * Flowable is used here, because otherwise concat map will try to consume
     * the entire infinite sequence.
     *
     * @param ghost the ghost to generate movement events for
     * @param index the index of the ghost in the entities vector
     * @return a flowable that will provide move timing events for the specified ghost
     * with the same behaviour as the NpcMoveTaks in jpacman 7.
     */
    public static Flowable<Integer> ghostMovementEvents(Ghost ghost, int index, Scheduler scheduler) {
        Flowable<Long> intervals = Flowable.<Long>generate(em -> em.onNext(ghost.getInterval()))
            .subscribeOn(scheduler);
        return intervals
            .concatMap(interval -> Flowable.defer(() -> Flowable.timer(interval, TimeUnit.MILLISECONDS, scheduler)))
            .map(t -> index);
    }

    public static Observable<Integer> ghostMovementEvents(Ghost ghost, int index) {
        return ghostMovementEvents(ghost, index, Schedulers.computation()).toObservable();
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
