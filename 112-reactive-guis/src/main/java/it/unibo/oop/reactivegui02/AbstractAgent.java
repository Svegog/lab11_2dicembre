package it.unibo.oop.reactivegui02;

/**
 * An abstract class to make different Agent.
 */
public abstract class AbstractAgent implements Runnable {

    /**
     * Variable needed to let the program run.
     */
    private volatile boolean stop;
    private int counter;

    /**
     * Defined for exercises purpose.
     */
    public AbstractAgent() { } // for error gave during compiling

    /**
     * Constructor for setting a starting value.
     * 
     * @param value the starting counter value
     */
    public AbstractAgent(final int value) {
        this.counter = value;
    }

    /**
     * Stop counting method.
     */
    public void stopCounting() {
        this.stop = true;
    }

    /**
     * Getter for the current counter.
     * 
     * @return the counter value
     */
    public int getCounter() {
        return this.counter;
    }

    /**
     * Get the state of the agent.
     * 
     * @return the state of the Thread
     */
    public boolean isWorking() {
        return this.stop;
    }
}
