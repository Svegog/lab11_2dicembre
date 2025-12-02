package it.unibo.oop.reactivegui02;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

/**
 * Second example of reactive GUI.
 */
public final class ConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentGUI.class);
    private final JLabel display = new JLabel();
    private transient AbstractAgent agent;

    /**
     * Builds a new CGUI.
     */
    public ConcurrentGUI() {
        super();
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
        final JButton up = new JButton("up");
        final JButton down = new JButton("down");
        final JButton stop = new JButton("stop");
        panel.add(up);
        panel.add(down);
        panel.add(stop);
        this.getContentPane().add(panel);
        this.setVisible(true);
        /*
         * Create the counter agent and start it. This is actually not so good:
         * thread management should be left to
         * java.util.concurrent.ExecutorService
         */
        agent = new UpAgent();
        new Thread(agent).start();
        /*
         * Register a listener that start the incrementing count.
         */
        up.addActionListener(e -> {
            agent.stopCounting();
            final int oldValue = agent.getCounter();
            agent = new UpAgent(oldValue);
            new Thread(agent).start();
        });
        /*
         * Register a listener that start the decrementing count.
         */
        down.addActionListener(e -> {
            agent.stopCounting();
            final int oldValue = agent.getCounter();
            agent = new DownAgent(oldValue);
            new Thread(agent).start();
        });
        /*
         * Register a listener that stop the app.
         */
        stop.addActionListener(e -> {
            agent.stopCounting();
            up.setEnabled(false);
            down.setEnabled(false);
            stop.setEnabled(false);
        });
    }

    /**
     * Implementation of the class agent for incrementing counter.
     */
    public class UpAgent extends AbstractAgent {

        private volatile boolean stop;
        private int counter;

        /**
         * For exercises purpose.
         */
        public UpAgent() {
            this.stop = false;
        }

        /**
         * Constructor for setting a value for the counter.
         * 
         * @param value the new value
         */
        UpAgent(final int value) {
            super(value);
        }

        /**
         * Count operation.
         */
        @Override
        public void run() {
            while (!this.stop) {
                    try {
                        // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                        final var nextText = Integer.toString(this.counter);
                        SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(nextText));
                        this.counter++;
                        Thread.sleep(100);
                    } catch (InvocationTargetException | InterruptedException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }
                }
        }
    }

    /**
     * Implementation of the class agent for decrementing counter.
     */
    private class DownAgent extends AbstractAgent {

        private volatile boolean stop;
        private int counter;

        /**
         * For exercises purpose.
         * 
         * @param value the new value
         */
        DownAgent(final int value) {
            super(value);
        }

        /**
         * Count operation.
         */
        @Override
        public void run() {
            while (!this.stop) {
                    try {
                        // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                        final var nextText = Integer.toString(this.counter);
                        SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(nextText));
                        this.counter--;
                        Thread.sleep(100);
                    } catch (InvocationTargetException | InterruptedException ex) {
                        LOGGER.error(ex.getMessage(), ex);
                    }
                }
        }
    }
}
