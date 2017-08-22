package nl.tudelft.jpacman.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.vavr.Function1;
import io.vavr.control.Option;
import nl.tudelft.jpacman.game.Game;
import nl.tudelft.jpacman.level.Entities;
import nl.tudelft.jpacman.level.Events;
import nl.tudelft.jpacman.level.Level;
import nl.tudelft.jpacman.ui.ScorePanel.ScoreFormatter;

/**
 * The default JPacMan UI frame. The PacManUI consists of the following
 * elements:
 *
 * <ul>
 * <li>A score panel at the top, displaying the score of the player(s).
 * <li>A board panel, displaying the current level, i.e. the board and all units
 * on it.
 * <li>A button panel, containing all buttons provided upon creation.
 * </ul>
 *
 * @author Jeroen Roosen 
 *
 */
public class PacManUI extends JFrame {

    /**
     * Default serialisation UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The desired frame rate interval for the graphics in milliseconds, 40
     * being 25 fps.
     */
    private static final int FRAME_INTERVAL = 40;

    /**
     * The panel displaying the player scores.
     */
    private final ScorePanel scorePanel;

    /**
     * The panel displaying the game.
     */
    private final BoardPanel boardPanel;
    private final Game game;

    /**
     * Creates a new UI for a JPac-Man game.
     *
     * @param game
     *            The game to play.
     * @param buttons
     *            The map of caption-to-action entries that will appear as
     *            buttons on the interface.
     * @param keyMappings
     *            The map of keyCode-to-action entries that will be added as key
     *            listeners to the interface.
     * @param scoreFormatter
     *            The formatter used to display the current score.
     */
    @SuppressWarnings("initialization") // requestFocusInWindow called before initialization ends
    public PacManUI(final Game game, final Map<String, Action> buttons,
                    final Map<Integer, Action> keyMappings,
                    ScoreFormatter scoreFormatter) {
        super("JPac-Man");
        this.game = game;
        assert game != null;
        assert buttons != null;
        assert keyMappings != null;

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel buttonPanel = new ButtonPanel(buttons, this);

        scorePanel = new ScorePanel(game.getPlayers());
        if (scoreFormatter != null) {
            scorePanel.setScoreFormatter(scoreFormatter);
        }

        boardPanel = new BoardPanel(game);

        Container contentPanel = getContentPane();
        contentPanel.setLayout(new BorderLayout());
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        contentPanel.add(scorePanel, BorderLayout.NORTH);
        contentPanel.add(boardPanel, BorderLayout.CENTER);

        pack();
    }

    /**
     * bridge the swing key events to an observable.
     * @return a cold infinite observable, with no respect for back pressure.
     */
    public Observable<KeyEvent> keyEvents() {
        Observable<KeyEvent> events = Observable.create(sub -> {
            addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    sub.onNext(e);
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    //no-op
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    //no-op
                }
            });
        });

        return events.observeOn(Schedulers.computation());
    }

    public static Observable<Entities> startEvents(Level level, Observable<KeyEvent> keyEvents) {
        Entities initialEntities = level.currentEntities();
        Observable<Function1<Entities, Option<Entities>>> entityEvents =
            Events.allEntityEvents(keyEvents, initialEntities);

        return entityEvents.scan(initialEntities, level::entityOperation);
    }

    /**
     * Starts the "engine", the thread that redraws the interface at set
     * intervals.
     */
    public void start() {
        Observable<Entities> states = startEvents(game.getLevel(), this.keyEvents());

        states
            .doOnNext(e -> System.out.println("entities updates" + e))
            .subscribe(game.getLevel()::setCurrentEntities);

        setVisible(true);
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(this::nextFrame, 0, FRAME_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * Draws the next frame, i.e. refreshes the scores and game.
     */
    private void nextFrame() {
        boardPanel.repaint();
        scorePanel.refresh();
    }
}
