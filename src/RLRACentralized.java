import java.util.*;

/**
 * Centralized RLRA Architecture
 *
 * Single controller makes all reconfiguration decisions.
 * Advantages: Simple, deterministic
 * Disadvantages: Single point of failure, potential bottleneck
 */
public class RLRACentralized extends BaseAgent {
    private Map<String, MonitorAgent> monitors;
    private Map<String, MachineAgent> machines;
    private List<String> reconfigurationHistory;

    public RLRACentralized(String id) {
        super(id, "RLRA_Centralized");
        this.monitors = new HashMap<>();
        this.machines = new HashMap<>();
        this.reconfigurationHistory = new ArrayList<>();
    }

    /**
     * Register a monitor agent.
     */
    public void registerMonitor(MonitorAgent monitor) {
        monitors.put(monitor.getId(), monitor);
    }

    /**
     * Register a machine agent.
     */
    public void registerMachine(MachineAgent machine) {
        machines.put(machine.getId(), machine);
    }

    @Override
    protected void handleMessage(Message message) {
        switch (message.getType()) {
            case RECONFIGURATION_REQUEST:
                handleReconfigurationRequest(message);
                break;
            case ACTION_RESULT:
                handleActionResult(message);
                break;
            default:
                break;
        }
    }

    private void handleReconfigurationRequest(Message message) {
        Map<String, Object> request = (Map<String, Object>) message.getPayload();
        String affectedMachine = (String) request.get("affectedMachine");
        String issue = (String) request.get("issue");

        System.out.println("[" + id + "] Processing reconfiguration request for " + affectedMachine);

        // Decide on reconfiguration strategy
        String strategy = decidePlan(affectedMachine, issue);

        // Execute the plan
        executePlan(strategy, affectedMachine);

        // Notify monitors
        notifyMonitors(strategy);
    }

    private void handleActionResult(Message message) {
        String result = (String) message.getPayload();
        System.out.println("[" + id + "] Machine reported: " + result);
    }

    /**
     * Centralized decision logic: analyze situation and choose strategy.
     */
    private String decidePlan(String affectedMachine, String issue) {
        MachineAgent machine = machines.get(affectedMachine);
        if (machine == null) {
            return "STRATEGY_UNKNOWN";
        }

        MachineState state = machine.getState();

        // Strategy selection based on issue type
        if (issue.contains("fail")) {
            // For machine failure, try bypass or reassignment
            if (hasBackupMachine(affectedMachine)) {
                return "STRATEGY_REASSIGNMENT";
            } else {
                return "STRATEGY_BYPASS";
            }
        } else if (issue.contains("slow")) {
            return "STRATEGY_ACCELERATION";
        } else {
            return "STRATEGY_BYPASS";
        }
    }

    /**
     * Execute the chosen reconfiguration plan.
     */
    private void executePlan(String strategy, String affectedMachine) {
        switch (strategy) {
            case "STRATEGY_BYPASS":
                executionBypass(affectedMachine);
                break;
            case "STRATEGY_REASSIGNMENT":
                executeReassignment(affectedMachine);
                break;
            case "STRATEGY_ACCELERATION":
                executeAcceleration(affectedMachine);
                break;
            default:
                break;
        }
        reconfigurationHistory.add(strategy + " for " + affectedMachine);
    }

    private void executionBypass(String affectedMachine) {
        System.out.println("[" + id + "] Executing BYPASS strategy for " + affectedMachine);
        MachineAgent machine = machines.get(affectedMachine);
        if (machine != null) {
            sendMessage(affectedMachine, Message.MessageType.EXECUTE_ACTION, "BYPASS");
        }
    }

    private void executeReassignment(String affectedMachine) {
        System.out.println("[" + id + "] Executing REASSIGNMENT strategy for " + affectedMachine);
        // Find backup machine
        for (String machineId : machines.keySet()) {
            MachineAgent machine = machines.get(machineId);
            if (!machineId.equals(affectedMachine) && machine.getState().getStatus() == MachineState.Status.OPERATIONAL) {
                System.out.println("[" + id + "] Reassigning work to " + machineId);
                break;
            }
        }
    }

    private void executeAcceleration(String affectedMachine) {
        System.out.println("[" + id + "] Executing ACCELERATION strategy for " + affectedMachine);
        sendMessage(affectedMachine, Message.MessageType.EXECUTE_ACTION, "ACCELERATE");
    }

    private boolean hasBackupMachine(String affectedMachine) {
        for (String machineId : machines.keySet()) {
            MachineAgent machine = machines.get(machineId);
            if (!machineId.equals(affectedMachine) && machine.getState().getStatus() == MachineState.Status.OPERATIONAL) {
                return true;
            }
        }
        return false;
    }

    private void notifyMonitors(String strategy) {
        for (MonitorAgent monitor : monitors.values()) {
            sendMessage(monitor.getId(), Message.MessageType.RECONFIGURATION_PLAN,
                       "Plan: " + strategy);
        }
    }

    public List<String> getReconfigurationHistory() {
        return new ArrayList<>(reconfigurationHistory);
    }
}
