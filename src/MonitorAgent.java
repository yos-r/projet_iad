import java.util.*;

/**
 * Agent that monitors machines on a specific site.
 */
public class MonitorAgent extends BaseAgent {
    private Map<String, MachineState> monitoredMachines;
    private String siteId;
    private String rlraId;

    public MonitorAgent(String monitorId, String siteId, String rlraId) {
        super(monitorId, "Monitor");
        this.siteId = siteId;
        this.rlraId = rlraId;
        this.monitoredMachines = new HashMap<>();
    }

    /**
     * Register a machine to be monitored.
     */
    public void registerMachine(String machineId, MachineState initialState) {
        monitoredMachines.put(machineId, initialState);
    }

    /**
     * Get all monitored machines.
     */
    public Map<String, MachineState> getMonitoredMachines() {
        return new HashMap<>(monitoredMachines);
    }

    @Override
    protected void handleMessage(Message message) {
        switch (message.getType()) {
            case STATE_UPDATE:
                handleStateUpdate(message);
                break;
            case RECONFIGURATION_PLAN:
                handleReconfigurationPlan(message);
                break;
            default:
                break;
        }
    }

    private void handleStateUpdate(Message message) {
        MachineState state = (MachineState) message.getPayload();
        monitoredMachines.put(state.getMachineId(), state);

        // Check if machine is in failure state
        if (state.getStatus() == MachineState.Status.FAILED) {
            System.out.println("[" + id + "] Detected failure in " + state.getMachineId() +
                              ": " + state.getErrorMessage());
            requestReconfiguration(state.getMachineId(), state.getErrorMessage());
        }
    }

    private void handleReconfigurationPlan(Message message) {
        String plan = (String) message.getPayload();
        System.out.println("[" + id + "] Received reconfiguration plan from RLRA: " + plan);
    }

    /**
     * Monitor polls all machines for status updates.
     */
    public void pollMachines() {
        for (String machineId : monitoredMachines.keySet()) {
            // In real system, would query machine agents
            // For now, just log activity
        }
    }

    /**
     * Detects anomalies and requests reconfiguration from RLRA.
     */
    private void requestReconfiguration(String affectedMachineId, String issue) {
        String request = "RECONFIGURATION_REQUEST: " + affectedMachineId + " - " + issue;
        Map<String, Object> payload = new HashMap<>();
        payload.put("affectedMachine", affectedMachineId);
        payload.put("issue", issue);
        payload.put("siteId", siteId);
        payload.put("affectedMachines", new ArrayList<>(monitoredMachines.keySet()));

        sendMessage(rlraId, Message.MessageType.RECONFIGURATION_REQUEST, payload);
        System.out.println("[" + id + "] Sent reconfiguration request to " + rlraId);
    }

    @Override
    public void step() {
        super.step();
        pollMachines();
    }
}
