/**
 * Abstract base class for all agents.
 */
public abstract class BaseAgent implements Agent {
    protected String id;
    protected String type;
    protected boolean running;
    protected MessageBroker messageBroker;

    public BaseAgent(String id, String type) {
        this.id = id;
        this.type = type;
        this.running = true;
        this.messageBroker = MessageBroker.getInstance();
        this.messageBroker.registerAgent(id);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void receiveMessage(Message message) {
        messageBroker.sendMessage(message);
    }

    @Override
    public void step() {
        // Process one message
        Message message = messageBroker.getMessage(id);
        if (message != null) {
            handleMessage(message);
        }
    }

    @Override
    public void stop() {
        running = false;
        messageBroker.unregisterAgent(id);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    /**
     * Subclasses override this to handle specific message types.
     */
    protected abstract void handleMessage(Message message);

    /**
     * Sends a message to another agent.
     */
    protected void sendMessage(String receiverId, Message.MessageType type, Object payload) {
        Message message = new Message(id, receiverId, type, payload);
        messageBroker.sendMessage(message);
    }
}
