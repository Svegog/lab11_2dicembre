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

        final var agent = new ModularAgent();
        new Thread(agent).start();
        up.addActionListener(e -> agent.setOperationSign(1));
        down.addActionListener(e -> agent.setOperationSign(-1));
        stop.addActionListener(e -> {
                agent.stopCounting();
                up.setEnabled(false);
                down.setEnabled(false);
                stop.setEnabled(false);
        });
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
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(nextText));
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
}
