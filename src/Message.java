/**
 * Represents a message in the multi-agent communication system.
 * Uses ACL (Agent Communication Language) protocol.
 */
public class Message {
    public enum MessageType {
        STATE_UPDATE,           // Monitor sends machine state
        RECONFIGURATION_REQUEST, // Monitor requests reconfiguration
        RECONFIGURATION_PLAN,   // Controller sends configuration plan
        EXECUTE_ACTION,         // Executor sends action to machine
        ACTION_RESULT,          // Machine reports action execution result
        CONFLICT_REPORT,        // Agent reports conflict
        CONFLICT_RESOLUTION,    // Supervisor sends conflict resolution
        HEARTBEAT               // Periodic status check
    }

    private String senderId;
    private String receiverId;
    private MessageType type;
    private Object payload;
    private long timestamp;

    public Message(String senderId, String receiverId, MessageType type, Object payload) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public MessageType getType() {
        return type;
    }

    public Object getPayload() {
        return payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "senderId='" + senderId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", type=" + type +
                ", payload=" + payload +
                ", timestamp=" + timestamp +
                '}';
    }
}
