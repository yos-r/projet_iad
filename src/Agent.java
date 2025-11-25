/**
 * Base interface for all agents in the system.
 */
public interface Agent {
    /**
     * Gets the unique identifier of the agent.
     */
    String getId();

    /**
     * Gets the type of the agent (Machine, Monitor, RLRA, etc.).
     */
    String getType();

    /**
     * Called when a message is received by this agent.
     */
    void receiveMessage(Message message);

    /**
     * Executes one step of the agent's behavior.
     */
    void step();

    /**
     * Stops the agent.
     */
    void stop();

    /**
     * Checks if the agent is still running.
     */
    boolean isRunning();
}
