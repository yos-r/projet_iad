import java.util.*;

/**
 * ConveyorAgent represents a conveyor belt system between factory sites.
 * Handles part transportation, timing, and failure scenarios.
 */
public class ConveyorAgent extends BaseAgent {
    public enum Status {
        MOVING,
        STOPPED,
        JAMMED,
        FAILED,
        MAINTENANCE
    }

    private String sourceLocation;
    private String destinationLocation;
    private int transportTime; // milliseconds
    private int maxCapacity; // number of parts
    private Queue<String> parts; // parts on conveyor
    private Status status;
    private double speed; // 0.0 to 1.0 (1.0 = normal)
    private String errorMessage;
    private int partsTransported;
    private long uptime; // milliseconds since last failure

    public ConveyorAgent(String id, String source, String destination, int transportTime, int capacity) {
        super(id, "Conveyor");
        this.sourceLocation = source;
        this.destinationLocation = destination;
        this.transportTime = transportTime;
        this.maxCapacity = capacity;
        this.parts = new LinkedList<>();
        this.status = Status.MOVING;
        this.speed = 1.0;
        this.partsTransported = 0;
        this.uptime = System.currentTimeMillis();
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public Status getStatus() {
        return status;
    }

    public int getPartsCount() {
        return parts.size();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public double getCapacityPercentage() {
        return (double) parts.size() / maxCapacity * 100;
    }

    public int getPartsTransported() {
        return partsTransported;
    }

    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - uptime) / 1000;
    }

    /**
     * Add a part to the conveyor
     */
    public boolean addPart(String partId) {
        if (status != Status.MOVING && status != Status.STOPPED) {
            return false; // Cannot add parts if conveyor is jammed or failed
        }
        if (parts.size() < maxCapacity) {
            parts.offer(partId);
            return true;
        }
        return false; // Capacity exceeded
    }

    /**
     * Remove a part from the conveyor (at destination)
     */
    public String removePart() {
        return parts.poll();
    }

    /**
     * Simulate part movement along conveyor
     */
    public void moveparts() {
        if (status != Status.MOVING) {
            return;
        }

        // Simulate transport time
        // In real system, would track part arrival times
        System.out.println("[" + id + "] Moving " + parts.size() + " parts from " +
                         sourceLocation + " to " + destinationLocation);

        if (!parts.isEmpty()) {
            String part = parts.peek();
            // Part would arrive after transportTime
        }
    }

    @Override
    protected void handleMessage(Message message) {
        switch (message.getType()) {
            case EXECUTE_ACTION:
                handleExecuteAction(message);
                break;
            case STATE_UPDATE:
                handleStateUpdate(message);
                break;
            default:
                break;
        }
    }

    private void handleExecuteAction(Message message) {
        String action = (String) message.getPayload();
        String[] parts_action = action.split(":");

        switch (parts_action[0].trim()) {
            case "STOP":
                status = Status.STOPPED;
                speed = 0.0;
                System.out.println("[" + id + "] STOPPED by command");
                break;
            case "START":
                if (status == Status.STOPPED) {
                    status = Status.MOVING;
                    speed = 1.0;
                    System.out.println("[" + id + "] STARTED");
                }
                break;
            case "REDUCE_SPEED":
                if (parts_action.length > 1) {
                    double factor = Double.parseDouble(parts_action[1]);
                    speed = Math.max(0.1, speed * factor);
                    System.out.println("[" + id + "] Speed reduced to " + (speed * 100) + "%");
                }
                break;
            case "CLEAR_JAM":
                if (status == Status.JAMMED) {
                    status = Status.MOVING;
                    errorMessage = null;
                    System.out.println("[" + id + "] Jam cleared");
                }
                break;
            case "EMERGENCY_STOP":
                status = Status.STOPPED;
                speed = 0.0;
                System.out.println("[" + id + "] EMERGENCY STOP activated");
                break;
        }
    }

    private void handleStateUpdate(Message message) {
        // Update conveyor state
    }

    /**
     * Simulate a jam/failure
     */
    public void simulateJam(String reason) {
        status = Status.JAMMED;
        errorMessage = reason;
        speed = 0.0;
        System.out.println("[" + id + "] JAMMED: " + reason);
    }

    /**
     * Simulate complete failure
     */
    public void simulateFailure(String reason) {
        status = Status.FAILED;
        errorMessage = reason;
        speed = 0.0;
        System.out.println("[" + id + "] FAILED: " + reason);
    }

    /**
     * Reset to operational state
     */
    public void reset() {
        status = Status.MOVING;
        speed = 1.0;
        errorMessage = null;
        uptime = System.currentTimeMillis();
        System.out.println("[" + id + "] Reset to operational state");
    }

    /**
     * Get conveyor statistics
     */
    public String getStatistics() {
        return String.format(
            "ConveyorAgent{" +
            "id='%s', source='%s', destination='%s', " +
            "status=%s, capacity=%.1f%%, partsTransported=%d, uptimeSeconds=%d}",
            id, sourceLocation, destinationLocation, status,
            getCapacityPercentage(), partsTransported, getUptimeSeconds()
        );
    }

    @Override
    public void step() {
        super.step();
        if (status == Status.MOVING && !parts.isEmpty()) {
            moveparts();
        }
    }
}
