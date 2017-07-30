package nl.tudelft.jpacman.level;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subscribers.TestSubscriber;
import io.vavr.collection.Array;
import nl.tudelft.jpacman.npc.ghost.Ghost;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventsTest {
    @Test
    void testBliep() {
        Ghost ghost = mock(Ghost.class);
        when(ghost.getInterval()).thenReturn(2L);
        int index = 0;
        TestScheduler scheduler = new TestScheduler();
//        Scheduler scheduler = Schedulers.io();
        Integer[] expected = Array.fill(150, () -> 0).toJavaArray(Integer.class);
        TestSubscriber<Integer> test = Events.ghostMovementEvents(ghost, index, scheduler).take(302, TimeUnit.MILLISECONDS, scheduler).test();
        scheduler.advanceTimeBy(305, TimeUnit.MILLISECONDS);
        scheduler.triggerActions();
        test.assertValues(expected);
    }

    @Test
    void bliep2() {
        TestScheduler scheduler = new TestScheduler();

        Observable<Long> tick = Observable.interval(1, TimeUnit.SECONDS, scheduler);
        Disposable toBeTested = Observable.fromIterable(Arrays.asList(1, 2, 3, 4, 5))
            .buffer(3)
            .zipWith(tick, (i, t) -> i)
            .subscribe(System.out::println);

        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
    }

}
