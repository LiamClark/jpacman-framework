package nl.tudelft.jpacman.level;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.vavr.collection.Stream;
import nl.tudelft.jpacman.Launcher;
import nl.tudelft.jpacman.ui.PacManUI;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class EventsTest {
    @Test
    void performance_of_ghost_events() throws InterruptedException {
        Launcher launcher = new Launcher();
        Level level = launcher.makeLevel();
        Observable<Entities> entitiesObservable = PacManUI.startEvents(level, Observable.empty());
        Observable<Long> allIntervals = entitiesObservable.timeInterval().map(t -> (t.time(TimeUnit.MILLISECONDS))).take(5000);
        List<Long> longs = allIntervals.toList().blockingGet();
        assertThat(Stream.ofAll(longs).max().get()).isLessThan(250);
    }

    @Test
    void performance_of_both_events() {
        Flowable<KeyEvent> keyEvents = Flowable.just(KeyEvent.VK_DOWN, KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT)
            .map(k -> {
                KeyEvent mock = mock(KeyEvent.class);
                when(mock.getKeyCode()).thenReturn(k);
                return mock;
            })
            .repeat();

        Launcher launcher = new Launcher();
        Level level = launcher.makeLevel();
        Observable<Entities> entitiesObservable = PacManUI.startEvents(level, keyEvents.toObservable());
        Observable<Long> allIntervals = entitiesObservable.timeInterval().map(t -> (t.time(TimeUnit.MILLISECONDS))).take(10_000);
        List<Long> longs = allIntervals.toList().blockingGet();
        assertThat(Stream.ofAll(longs).average().get()).isLessThan(16);
    }
}
