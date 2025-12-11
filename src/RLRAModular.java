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
        
        System.out.println("\n[ACL] FROM " + message.getSenderId() + 
                           " TO " + id + 
                           " TYPE REQUEST CONTENT '" + 
                           request.get("affectedMachine") + "'");
        
        System.out.println("[" + id + "] Received RECONFIGURATION_REQUEST - Processing...");
        monitorModule.receiveRequest(request);

        // Step 2: Learner makes decision based on current state
        String strategy = learnerModule.decidePlan(request);
        System.out.println("[" + id + "] Selected strategy: " + strategy);

        // Step 3: Executor carries out the plan
        executorModule.executePlan(strategy, (String) request.get("affectedMachine"), request);
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
            
            // Déterminer le scénario
            ReconfigurationScenario scenario = detectScenario(issue);
            System.out.println("[" + id + "] Detected scenario: " + scenario.name());

            String strategy;
            
            if (scenario == ReconfigurationScenario.MACHINE_FAILURE) {
                if (issue.contains("Spindle") || issue.contains("belt")) {
                    strategy = "STRATEGY_BYPASS";
                    System.out.println("[" + id + "] Machine failure detected (Spindle/Belt)");
                    System.out.println("[" + id + "] → Analyzing alternatives...");
                    System.out.println("[" + id + "] → Decision: BYPASS available");
                } else {
                    strategy = "STRATEGY_REASSIGNMENT";
                    System.out.println("[" + id + "] Machine failure detected (Gripper)");
                    System.out.println("[" + id + "] → Analyzing alternatives...");
                    System.out.println("[" + id + "] → Decision: REASSIGNMENT available");
                }
            } else if (scenario == ReconfigurationScenario.PRODUCTION_PEAK) {
                strategy = "STRATEGY_ACCELERATION_MODE";
                System.out.println("[" + id + "] Production peak detected");
                System.out.println("[" + id + "] → Analyzing capacity...");
                System.out.println("[" + id + "] → Decision: ACTIVATE ACCELERATION");
            } else if (scenario == ReconfigurationScenario.PRODUCT_CHANGE) {
                strategy = "STRATEGY_PRODUCT_RECONFIGURATION";
                System.out.println("[" + id + "] Product change detected");
                System.out.println("[" + id + "] → Analyzing requirements...");
                System.out.println("[" + id + "] → Decision: RECONFIGURE MACHINES");
            } else {
                strategy = "STRATEGY_BYPASS";
            }

            decisionHistory.add(strategy);
            System.out.println("[" + id + "] ✓ Final decision: " + strategy);
            return strategy;
        }

        private ReconfigurationScenario detectScenario(String issue) {
            if (issue.contains("peak") || issue.contains("urgent")) {
                return ReconfigurationScenario.PRODUCTION_PEAK;
            } else if (issue.contains("product") || issue.contains("change")) {
                return ReconfigurationScenario.PRODUCT_CHANGE;
            } else {
                return ReconfigurationScenario.MACHINE_FAILURE;
            }
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

        public void executePlan(String strategy, String affectedMachine, Map<String, Object> request) {
            System.out.println("[" + id + "] Starting execution of plan: " + strategy);
            System.out.println("[" + id + "] Target machine: " + affectedMachine);

            switch (strategy) {
                case "STRATEGY_BYPASS":
                    System.out.println("[" + id + "] → Sending BYPASS command to " + affectedMachine);
                    System.out.println("[" + id + "] → Rerouting traffic to downstream machines");
                    if (machines.containsKey(affectedMachine)) {
                        machines.get(affectedMachine).receiveMessage(
                            new Message(id, affectedMachine, Message.MessageType.EXECUTE_ACTION, "BYPASS"));
                    }
                    executionHistory.add("BYPASS_" + affectedMachine);
                    break;

                case "STRATEGY_REASSIGNMENT":
                    System.out.println("[" + id + "] → Finding operational alternative machine");
                    for (MachineAgent machine : machines.values()) {
                        if (!machine.getId().equals(affectedMachine) &&
                            machine.getState().getStatus() == MachineState.Status.OPERATIONAL) {
                            System.out.println("[" + id + "] → Reassigning work to " + machine.getId());
                            System.out.println("[" + id + "] → Notifying " + machine.getId() + " of new responsibilities");
                            executionHistory.add("REASSIGNMENT_TO_" + machine.getId());
                            break;
                        }
                    }
                    break;

                case "STRATEGY_ACCELERATION_MODE":
                    System.out.println("[" + id + "] → Activating acceleration mode on all machines");
                    for (MachineAgent machine : machines.values()) {
                        System.out.println("[" + id + "] → Increasing speed of " + machine.getId());
                        machine.receiveMessage(
                            new Message(id, machine.getId(), Message.MessageType.EXECUTE_ACTION, "ACCELERATE"));
                    }
                    executionHistory.add("ACCELERATION_MODE_ACTIVE");
                    break;

                case "STRATEGY_PRODUCT_RECONFIGURATION":
                    System.out.println("[" + id + "] → Reconfiguring all machines for new product");
                    for (MachineAgent machine : machines.values()) {
                        System.out.println("[" + id + "] → Updating program for " + machine.getId());
                    }
                    executionHistory.add("PRODUCT_RECONFIGURATION_" + (String)request.getOrDefault("newProduct", "BETA"));
                    break;

                case "STRATEGY_ACCELERATION":
                    System.out.println("[" + id + "] → Sending ACCELERATE command to " + affectedMachine);
                    if (machines.containsKey(affectedMachine)) {
                        machines.get(affectedMachine).receiveMessage(
                            new Message(id, affectedMachine, Message.MessageType.EXECUTE_ACTION, "ACCELERATE"));
                    }
                    executionHistory.add("ACCELERATION_" + affectedMachine);
                    break;
            }

            System.out.println("[" + id + "] ✓ Execution completed");
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
