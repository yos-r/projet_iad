import java.util.*;

/**
 * Modular (Composite) RLRA Architecture
 *
 * RLRA is divided into three modules:
 * 1. Monitor: Collects state from site monitors
 * 2. Learner: Makes reconfiguration decisions
 * 3. Executor: Executes the calculated plans
 *
 * Advantages: Better separation of concerns, easier to test modules independently
 * Disadvantages: More complex communication between modules
 */
public class RLRAModular extends BaseAgent {
    private MonitorModule monitorModule;
    private LearnerModule learnerModule;
    private ExecutorModule executorModule;

    public RLRAModular(String id) {
        super(id, "RLRA_Modular");
        this.monitorModule = new MonitorModule(id + "_Monitor");
        this.learnerModule = new LearnerModule(id + "_Learner");
        this.executorModule = new ExecutorModule(id + "_Executor");
    }

    /**
     * Register monitors with the monitor module.
     */
    public void registerMonitor(MonitorAgent monitor) {
        monitorModule.registerMonitor(monitor);
    }

    /**
     * Register machines with the executor module.
     */
    public void registerMachine(MachineAgent machine) {
        executorModule.registerMachine(machine);
    }

    @Override
    protected void handleMessage(Message message) {
        // Route messages through modules
        switch (message.getType()) {
            case RECONFIGURATION_REQUEST:
                handleReconfigurationRequest(message);
                break;
            case ACTION_RESULT:
                executorModule.handleActionResult(message);
                break;
            default:
                break;
        }
    }

    private void handleReconfigurationRequest(Message message) {
        // Step 1: Monitor module receives the request
        Map<String, Object> request = (Map<String, Object>) message.getPayload();
        monitorModule.receiveRequest(request);

        // Step 2: Learner makes decision based on current state
        String strategy = learnerModule.decidePlan(request);

        // Step 3: Executor carries out the plan
        executorModule.executePlan(strategy, (String) request.get("affectedMachine"));
    }

    @Override
    public void step() {
        super.step();
        monitorModule.step();
        learnerModule.step();
        executorModule.step();
    }

    public List<String> getExecutionHistory() {
        return executorModule.getExecutionHistory();
    }

    // ==================== Inner Classes ====================

    /**
     * Monitor Module: Collects and maintains state information
     */
    private static class MonitorModule {
        private String id;
        private Map<String, Object> currentState;
        private List<Map<String, Object>> receivedRequests;

        public MonitorModule(String id) {
            this.id = id;
            this.currentState = new HashMap<>();
            this.receivedRequests = new ArrayList<>();
        }

        public void registerMonitor(MonitorAgent monitor) {
            currentState.put(monitor.getId(), monitor.getMonitoredMachines());
        }

        public void receiveRequest(Map<String, Object> request) {
            receivedRequests.add(request);
            System.out.println("[" + id + "] Received request: " + request.get("affectedMachine"));
        }

        public Map<String, Object> getCurrentState() {
            return new HashMap<>(currentState);
        }

        public void step() {
            // Monitor can update state periodically
        }
    }

    /**
     * Learner Module: Makes reconfiguration decisions
     */
    private static class LearnerModule {
        private String id;
        private List<String> decisionHistory;

        public LearnerModule(String id) {
            this.id = id;
            this.decisionHistory = new ArrayList<>();
        }

        public String decidePlan(Map<String, Object> request) {
            String affectedMachine = (String) request.get("affectedMachine");
            String issue = (String) request.get("issue");

            System.out.println("[" + id + "] Analyzing situation for " + affectedMachine);

            String strategy;
            if (issue.contains("fail")) {
                strategy = "STRATEGY_REASSIGNMENT";
            } else if (issue.contains("slow")) {
                strategy = "STRATEGY_ACCELERATION";
            } else {
                strategy = "STRATEGY_BYPASS";
            }

            decisionHistory.add(strategy);
            System.out.println("[" + id + "] Decision: " + strategy);
            return strategy;
        }

        public void step() {
            // Learner can refine its decision logic
        }
    }

    /**
     * Executor Module: Carries out the reconfiguration plan
     */
    private static class ExecutorModule {
        private String id;
        private Map<String, MachineAgent> machines;
        private List<String> executionHistory;

        public ExecutorModule(String id) {
            this.id = id;
            this.machines = new HashMap<>();
            this.executionHistory = new ArrayList<>();
        }

        public void registerMachine(MachineAgent machine) {
            machines.put(machine.getId(), machine);
        }

        public void executePlan(String strategy, String affectedMachine) {
            System.out.println("[" + id + "] Executing plan: " + strategy + " for " + affectedMachine);

            switch (strategy) {
                case "STRATEGY_BYPASS":
                    machines.get(affectedMachine).receiveMessage(
                        new Message(id, affectedMachine, Message.MessageType.EXECUTE_ACTION, "BYPASS"));
                    break;
                case "STRATEGY_REASSIGNMENT":
                    // Reassign to another operational machine
                    for (MachineAgent machine : machines.values()) {
                        if (!machine.getId().equals(affectedMachine) &&
                            machine.getState().getStatus() == MachineState.Status.OPERATIONAL) {
                            System.out.println("[" + id + "] Reassigning work to " + machine.getId());
                            break;
                        }
                    }
                    break;
                case "STRATEGY_ACCELERATION":
                    machines.get(affectedMachine).receiveMessage(
                        new Message(id, affectedMachine, Message.MessageType.EXECUTE_ACTION, "ACCELERATE"));
                    break;
            }

            executionHistory.add(strategy);
        }

        public void handleActionResult(Message message) {
            System.out.println("[" + id + "] " + message.getPayload());
        }

        public List<String> getExecutionHistory() {
            return new ArrayList<>(executionHistory);
        }

        public void step() {
            // Executor can monitor execution progress
        }
    }
}
