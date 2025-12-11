import java.util.*;

/**
 * TransportAgent: Gère les transports entre les sites
 * 
 * Responsabilités:
 * - Surveiller l'état du transport T1
 * - Détecter les congestions du buffer
 * - Signaler les anomalies aux moniteurs
 * - Participer aux reconfigurations
 * 
 * Scénarios gérés:
 * 1. Transport normal: ajouter/retirer des pièces du buffer
 * 2. Buffer congestion: alerte quand buffer > 80%
 * 3. Buffer overflow: alerte quand buffer est plein
 * 4. Transport failure: simuler une panne (ceinture coincée, etc.)
 */
public class TransportAgent extends BaseAgent {
    private String sourceSite;
    private String destinationSite;
    private int transportTime;      // en secondes
    private int bufferCapacity;
    private int currentBufferLoad;
    private boolean isOperational;
    private String lastFailureReason;

    public TransportAgent(String id, String sourceSite, String destSite, int tTime, int capacity) {
        super(id, "Transport");
        this.sourceSite = sourceSite;
        this.destinationSite = destSite;
        this.transportTime = tTime;
        this.bufferCapacity = capacity;
        this.currentBufferLoad = 0;
        this.isOperational = true;
        this.lastFailureReason = null;
    }

    /**
     * Ajouter des pièces au buffer (depuis M1/M2)
     */
    public synchronized boolean addToBuffer(int quantity) {
        if (!isOperational) {
            System.out.println("[" + id + "] ⚠ Transport FAILED - Cannot add parts");
            return false;
        }

        if (currentBufferLoad + quantity <= bufferCapacity) {
            currentBufferLoad += quantity;
            System.out.println("[" + id + "] Added " + quantity + " parts (buffer: " + 
                             currentBufferLoad + "/" + bufferCapacity + ")");
            
            // Check buffer congestion
            int percentage = (100 * currentBufferLoad / bufferCapacity);
            if (percentage > 80 && percentage <= 95) {
                System.out.println("[" + id + "] ⚠ WARNING: Buffer at " + percentage + "% capacity");
                notifyBufferCongestion();
            } else if (percentage > 95) {
                System.out.println("[" + id + "] 🔴 ALERT: Buffer CRITICAL at " + percentage + "% capacity");
            }
            return true;
        } else {
            System.out.println("[" + id + "] ✗ Buffer FULL - Cannot add parts (buffer: " + 
                             currentBufferLoad + "/" + bufferCapacity + ")");
            notifyBufferOverflow();
            return false;
        }
    }

    /**
     * Retirer des pièces du buffer (vers M3/M4)
     */
    public synchronized int removeFromBuffer(int quantity) {
        if (!isOperational) {
            System.out.println("[" + id + "] ⚠ Transport FAILED - Cannot remove parts");
            return 0;
        }

        int actualQuantity = Math.min(quantity, currentBufferLoad);
        currentBufferLoad -= actualQuantity;
        System.out.println("[" + id + "] Removed " + actualQuantity + " parts (buffer: " + 
                         currentBufferLoad + "/" + bufferCapacity + ")");
        return actualQuantity;
    }

    /**
     * Simuler une panne du transport
     */
    public void simulateFailure(String reason) {
        isOperational = false;
        lastFailureReason = reason;
        
        System.out.println("\n[" + id + "] 🔴 TRANSPORT FAILED");
        System.out.println("[" + id + "] Failure reason: " + reason);
        System.out.println("[" + id + "] Buffer locked at " + currentBufferLoad + " parts");
        System.out.println("[" + id + "] Production flow interrupted: " + sourceSite + " → " + destinationSite);
        
        // Notifier le monitor
        notifyFailure();
    }

    /**
     * Réparer le transport
     */
    public void repair() {
        isOperational = true;
        lastFailureReason = null;
        System.out.println("[" + id + "] ✓ Transport REPAIRED and operational");
        System.out.println("[" + id + "] Buffer released: " + currentBufferLoad + " parts can now move");
    }

    private void notifyBufferCongestion() {
        Map<String, Object> alert = new HashMap<>();
        alert.put("transportId", id);
        alert.put("bufferLoad", currentBufferLoad);
        alert.put("capacity", bufferCapacity);
        alert.put("percentage", (100 * currentBufferLoad / bufferCapacity));
        alert.put("issue", "Buffer congestion detected");
        
        System.out.println("[ACL] FROM " + id + " TO Monitor_SiteA " + 
                         "TYPE RECONFIGURATION_REQUEST CONTENT 'BUFFER_CONGESTION_" + 
                         (100 * currentBufferLoad / bufferCapacity) + "%'");
    }

    private void notifyBufferOverflow() {
        Map<String, Object> alert = new HashMap<>();
        alert.put("transportId", id);
        alert.put("bufferLoad", currentBufferLoad);
        alert.put("capacity", bufferCapacity);
        alert.put("issue", "Buffer overflow - production must pause");
        
        System.out.println("[ACL] FROM " + id + " TO Monitor_SiteA " + 
                         "TYPE RECONFIGURATION_REQUEST CONTENT 'BUFFER_OVERFLOW'");
    }

    private void notifyFailure() {
        Map<String, Object> failure = new HashMap<>();
        failure.put("transportId", id);
        failure.put("reason", lastFailureReason);
        failure.put("sourceSite", sourceSite);
        failure.put("destinationSite", destinationSite);
        
        System.out.println("[ACL] FROM " + id + " TO Monitor_SiteA " + 
                         "TYPE RECONFIGURATION_REQUEST CONTENT 'TRANSPORT_FAILURE: " + lastFailureReason + "'");
    }

    @Override
    protected void handleMessage(Message message) {
        if (message.getType() == Message.MessageType.EXECUTE_ACTION) {
            String action = (String) message.getPayload();
            if (action.equals("REPAIR")) {
                repair();
            } else if (action.contains("REDUCE_RATE")) {
                System.out.println("[" + id + "] Reducing transport speed to 50%");
            }
        }
    }

    public boolean isOperational() {
        return isOperational;
    }

    public int getBufferLoad() {
        return currentBufferLoad;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    public int getBufferPercentage() {
        return (100 * currentBufferLoad / bufferCapacity);
    }

    public String getLastFailureReason() {
        return lastFailureReason;
    }

    @Override
    public void step() {
        super.step();
        // Transport can periodically report buffer status
        int percentage = getBufferPercentage();
        if (percentage > 90) {
            System.out.println("[" + id + "] [ALERT] Buffer critically full: " + percentage + "%");
        }
    }
}
