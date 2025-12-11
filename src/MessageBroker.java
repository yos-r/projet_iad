import java.util.*;
import java.util.concurrent.*;

/**
 * Central message broker for inter-agent communication.
 */
public class MessageBroker {
    private static final MessageBroker instance = new MessageBroker();
    private Map<String, Queue<Message>> mailboxes;
    private List<Message> messageLog;
    private boolean running;

    private MessageBroker() {
        this.mailboxes = new ConcurrentHashMap<>();
        this.messageLog = Collections.synchronizedList(new ArrayList<>());
        this.running = true;
    }

    public static MessageBroker getInstance() {
        return instance;
    }

    /**
     * Registers an agent with the message broker.
     */
    public void registerAgent(String agentId) {
        mailboxes.putIfAbsent(agentId, new ConcurrentLinkedQueue<>());
    }

    /**
     * Unregisters an agent from the message broker.
     */
    public void unregisterAgent(String agentId) {
        mailboxes.remove(agentId);
    }

    /**
     * Sends a message from one agent to another.
     */
    public void sendMessage(Message message) {
        if (!running) return;

        Queue<Message> mailbox = mailboxes.get(message.getReceiverId());
        if (mailbox != null) {
            mailbox.offer(message);
            messageLog.add(message);
        }
    }

    /**
     * Retrieves the next message for an agent.
     */
    public Message getMessage(String agentId) {
        Queue<Message> mailbox = mailboxes.get(agentId);
        if (mailbox != null) {
            return mailbox.poll();
        }
        return null;
    }

    /**
     * Checks if an agent has pending messages.
     */
    public boolean hasMessages(String agentId) {
        Queue<Message> mailbox = mailboxes.get(agentId);
        return mailbox != null && !mailbox.isEmpty();
    }

    /**
     * Gets all messages for a specific agent without removing them.
     */
    public List<Message> peekMessages(String agentId) {
        Queue<Message> mailbox = mailboxes.get(agentId);
        if (mailbox != null) {
            return new ArrayList<>(mailbox);
        }
        return new ArrayList<>();
    }

    /**
     * Gets the message log for debugging.
     */
    public List<Message> getMessageLog() {
        return new ArrayList<>(messageLog);
    }

    /**
     * Clears the message log.
     */
    public void clearMessageLog() {
        messageLog.clear();
    }

    /**
     * Stops the message broker.
     */
    public void stop() {
        running = false;
        mailboxes.clear();
    }

    /**
     * Restarts the message broker for a new simulation.
     */
    public void restart() {
        running = true;
        mailboxes.clear();
        messageLog.clear();
    }

    /**
     * Prints message statistics.
     */
    public void printStatistics() {
        System.out.println("=== Message Broker Statistics ===");
        System.out.println("Total messages logged: " + messageLog.size());
        System.out.println("Active agents: " + mailboxes.size());
        messageLog.stream()
                .collect(java.util.stream.Collectors.groupingBy(Message::getType, java.util.stream.Collectors.counting()))
                .forEach((type, count) -> System.out.println("  " + type + ": " + count));
    }
}
