package it.unibo.oop.reactivegui03;

import java.io.Serial;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.unibo.oop.JFrameUtil;

/**
 * Third experiment with reactive gui.
 */
public final class AnotherConcurrentGUI extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AnotherConcurrentGUI.class);
    private static final long TIMETOPASS = 10_000L;
    private final JLabel display = new JLabel();
    private volatile boolean canRun;

    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");

    /**
     * Builds a new CGUI.
     */
    public AnotherConcurrentGUI() {
        super();
        this.canRun = true;
        JFrameUtil.dimensionJFrame(this);
        final JPanel panel = new JPanel();
        panel.add(display);
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
        final var agent = new ModularAgent();
        new Thread(agent).start();
        final var timer = new Timer();
        new Thread(timer).start();
        /*
         * Register a listener that start the incrementing count.
         */
        up.addActionListener(e -> agent.setOperationSign(1));
        /*
         * Register a listener that start the decrementing count.
         */
        down.addActionListener(e -> agent.setOperationSign(-1));
        /*
         * Register a listener that stop the app.
         */
        stop.addActionListener(e -> {
            agent.stopCounting();
            blockAllButton();
        });
    }

    private void blockAllButton() {
        up.setEnabled(false);
        down.setEnabled(false);
        stop.setEnabled(false);
    }

    /**
     * A modular agent that permit to choose the counter mode.
     */
    private class ModularAgent implements Runnable {

        private volatile boolean stop;
        private volatile int operationMode;
        private int counter;

        /**
         * Default mode is incremental.
         */
        ModularAgent() {
            this.operationMode = 1;
        }

        /**
         * Select a new mode and a new value to start.
         * 
         * @param mode can be incremental or decremental
         */
        public void setOperationSign(final int mode) {
            this.operationMode = mode;
        }

        /**
         * Same as the exercises before but with a little twist.
         */
        @Override
        public void run() {
            while (!this.stop && canRun) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
                    // this.counter += this.operationMode;
                    if (operationMode > 0) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
        }

        /**
         * Stop the counter.
         */
        public void stopCounting() {
            this.stop = true;
        }
    }

    private final class Timer implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(TIMETOPASS);
                canRun = false;
                blockAllButton();
            } catch (final InterruptedException ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }
}
