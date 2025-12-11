import java.util.*;

/**
 * Distributed RLRA Architecture
 *
 * Decision-making is distributed across sites:
 * - Each site has a Coordinator that manages local conflicts
 * - A global Supervisor has a complete view and resolves inter-site conflicts
 * - Machines make local decisions within their constraints
 *
 * Advantages: Scalable, resilient to partial failures, faster local decisions
 * Disadvantages: Complex coordination, potential inconsistencies
 */
public class RLRADistributed extends BaseAgent {
    private Map<String, SiteCoordinator> coordinators;
    private Supervisor supervisor;

    public RLRADistributed(String id) {
        super(id, "RLRA_Distributed");
        this.coordinators = new HashMap<>();
        this.supervisor = new Supervisor(id + "_Supervisor");
    }

    /**
     * Create and register a site coordinator.
     */
    public SiteCoordinator createSiteCoordinator(String siteId) {
        SiteCoordinator coordinator = new SiteCoordinator(siteId + "_Coordinator", siteId);
        coordinators.put(siteId, coordinator);
        supervisor.registerCoordinator(coordinator);
        return coordinator;
    }

    /**
     * Get a site coordinator.
     */
    public SiteCoordinator getCoordinator(String siteId) {
        return coordinators.get(siteId);
    }

    @Override
    protected void handleMessage(Message message) {
        switch (message.getType()) {
            case RECONFIGURATION_REQUEST:
                handleReconfigurationRequest(message);
                break;
            case CONFLICT_REPORT:
                handleConflictReport(message);
                break;
            default:
                break;
        }
    }

    private void handleReconfigurationRequest(Message message) {
        Map<String, Object> request = (Map<String, Object>) message.getPayload();
        String siteId = (String) request.get("siteId");

        System.out.println("\n[ACL] FROM " + message.getSenderId() + 
                           " TO " + id + 
                           " TYPE REQUEST CONTENT '" + 
                           request.get("affectedMachine") + "'");
        System.out.println("[" + id + "] Received RECONFIGURATION_REQUEST - Delegating to coordinator for " + siteId);

        SiteCoordinator coordinator = coordinators.get(siteId);
        if (coordinator != null) {
            coordinator.handleReconfigurationRequest(request);
        } else {
            System.out.println("[" + id + "] ERROR: No coordinator found for " + siteId);
        }
    }

    private void handleConflictReport(Message message) {
        Map<String, Object> conflict = (Map<String, Object>) message.getPayload();
        String coordinatorId = (String) conflict.get("coordinatorId");
        String siteId = (String) conflict.get("siteId");
        
        System.out.println("\n[" + id + "] Received CONFLICT_REPORT from " + coordinatorId);
        supervisor.handleConflictReport(conflict);
        
        String resolution = supervisor.resolveConflict(conflict);
        System.out.println("[" + id + "] ✓ Global decision made: " + resolution);
        System.out.println("[" + id + "] Sending resolution back to coordinator for " + siteId);

        SiteCoordinator coordinator = coordinators.get(siteId);
        if (coordinator != null) {
            ACLMessageLogger.logConflictResolution("Supervisor", coordinatorId, resolution);
            sendMessage(coordinator.getId(), Message.MessageType.CONFLICT_RESOLUTION, resolution);
        }
    }

    @Override
    public void step() {
        super.step();
        // Each coordinator processes locally
        for (SiteCoordinator coordinator : coordinators.values()) {
            coordinator.step();
        }
        supervisor.step();
    }

    public Supervisor getSupervisor() {
        return supervisor;
    }

    // ==================== Inner Classes ====================

    /**
     * Site Coordinator: Manages local decisions and conflicts at a single site
     */
    public static class SiteCoordinator extends BaseAgent {
        private String siteId;
        private Map<String, MachineAgent> localMachines;
        private List<String> decisions;
        private List<String> reconfigurationHistory;

        public SiteCoordinator(String id, String siteId) {
            super(id, "Coordinator");
            this.siteId = siteId;
            this.localMachines = new HashMap<>();
            this.decisions = new ArrayList<>();
            this.reconfigurationHistory = new ArrayList<>();
        }

        public void registerMachine(MachineAgent machine) {
            localMachines.put(machine.getId(), machine);
        }

        @Override
        protected void handleMessage(Message message) {
            switch (message.getType()) {
                case RECONFIGURATION_REQUEST:
                    handleReconfigurationRequest((Map<String, Object>) message.getPayload());
                    break;
                case CONFLICT_RESOLUTION:
                    handleConflictResolution((String) message.getPayload());
                    break;
                default:
                    break;
            }
        }

        public void handleReconfigurationRequest(Map<String, Object> request) {
            String affectedMachine = (String) request.get("affectedMachine");
            String issue = (String) request.get("issue");
            
            System.out.println("\n[" + id + "] ===== LOCAL DECISION PROCESS =====");
            System.out.println("[" + id + "] Received failure alert for " + affectedMachine);
            System.out.println("[" + id + "] Issue: " + issue);
            System.out.println("[" + id + "] Analyzing local resources...");

            reconfigurationHistory.add("LOCAL_ANALYSIS for " + affectedMachine);

            // Make local decision
            String strategy = decideLocalStrategy(affectedMachine);
            System.out.println("[" + id + "] Proposed local strategy: " + strategy);
            decisions.add(strategy);

            // Check for local conflicts
            if (hasLocalConflict(affectedMachine, strategy)) {
                System.out.println("[" + id + "] ⚠ LOCAL CONFLICT DETECTED");
                System.out.println("[" + id + "] Cannot solve locally → Escalating to Supervisor");
                reportConflict(affectedMachine, strategy);
            } else {
                System.out.println("[" + id + "] ✓ Local solution viable");
                executeLocalDecision(strategy, affectedMachine);
            }
        }

        private void handleConflictResolution(String resolution) {
            System.out.println("\n[" + id + "] Received Supervisor resolution: " + resolution);
            System.out.println("[" + id + "] ? Applying global decision...");
            
            reconfigurationHistory.add("GLOBAL_RESOLUTION " + resolution + " by Supervisor");
            
            if (resolution.contains("BYPASS")) {
                System.out.println("[" + id + "] Disabling local failed machines");
                System.out.println("[" + id + "] Rerouting production to alternative site");
            } else if (resolution.contains("APPROVED")) {
                System.out.println("[" + id + "] Supervisor approved local strategy");
                System.out.println("[" + id + "] Executing approved plan");
            }
            
            System.out.println("[" + id + "] ✓ Global decision applied");
        }

        private String decideLocalStrategy(String affectedMachine) {
            MachineAgent machine = localMachines.get(affectedMachine);
            if (machine != null && machine.getState().getStatus() == MachineState.Status.FAILED) {
                return "STRATEGY_LOCAL_BYPASS";
            }
            return "STRATEGY_LOCAL_ACCELERATION";
        }

        private boolean hasLocalConflict(String affectedMachine, String strategy) {
            // Check if decision conflicts with other machines on this site
            int operationalCount = 0;
            for (MachineAgent machine : localMachines.values()) {
                if (machine.getState().getStatus() == MachineState.Status.OPERATIONAL) {
                    operationalCount++;
                }
            }
            // Conflict if only one machine left operational
            return operationalCount <= 1 && strategy.contains("BYPASS");
        }

        private void reportConflict(String affectedMachine, String strategy) {
            System.out.println("[" + id + "] ⚠ LOCAL CONFLICT DETECTED");
            System.out.println("[" + id + "] Cannot solve locally → Escalating to Supervisor");
            
            ACLMessageLogger.logConflictReport(id, "Supervisor", affectedMachine, 
                                              "local strategy not sufficient");
            
            Map<String, Object> conflict = new HashMap<>();
            conflict.put("siteId", siteId);
            conflict.put("affectedMachine", affectedMachine);
            conflict.put("strategy", strategy);
            conflict.put("coordinatorId", id);

            System.out.println("[" + id + "] Sending CONFLICT_REPORT to Supervisor");
            System.out.println("[" + id + "] Conflict details: Machine=" + affectedMachine + 
                             " Strategy=" + strategy);
            
            Message msg = new Message(id, "RLRA_Main", Message.MessageType.CONFLICT_REPORT, conflict);
            messageBroker.sendMessage(msg);
        }

        private void executeLocalDecision(String strategy, String affectedMachine) {
            System.out.println("[" + id + "] ✓ Executing local decision: " + strategy);
            System.out.println("[" + id + "] Sending command to " + affectedMachine);
            MachineAgent machine = localMachines.get(affectedMachine);
            if (machine != null) {
                sendMessage(affectedMachine, Message.MessageType.EXECUTE_ACTION,
                           strategy.replace("LOCAL_", ""));
            }
        }

        public String getSiteId() {
            return siteId;
        }

        public List<String> getDecisions() {
            return new ArrayList<>(decisions);
        }

        public List<String> getReconfigurationHistory() {
            return new ArrayList<>(reconfigurationHistory);
        }
    }

    /**
     * Supervisor: Has global view and resolves inter-site conflicts
     */
    public static class Supervisor {
        private String id;
        private Map<String, SiteCoordinator> coordinators;
        private List<String> resolutions;

        public Supervisor(String id) {
            this.id = id;
            this.coordinators = new HashMap<>();
            this.resolutions = new ArrayList<>();
        }

        public void registerCoordinator(SiteCoordinator coordinator) {
            coordinators.put(coordinator.getSiteId(), coordinator);
        }

        public void handleConflictReport(Map<String, Object> conflict) {
            String coordinatorId = (String) conflict.get("coordinatorId");
            String affectedMachine = (String) conflict.get("affectedMachine");
            String siteId = (String) conflict.get("siteId");
            String localStrategy = (String) conflict.get("strategy");

            System.out.println("\n[" + id + "] Received CONFLICT_REPORT from " + coordinatorId);
            System.out.println("[" + id + "] Machine: " + affectedMachine + " at site: " + siteId);
            System.out.println("[" + id + "] Local strategy insufficient: " + localStrategy);
        }

        public String resolveConflict(Map<String, Object> conflict) {
            String affectedMachine = (String) conflict.get("affectedMachine");
            String siteId = (String) conflict.get("siteId");
            String coordinatorId = (String) conflict.get("coordinatorId");

            System.out.println("\n[" + id + "] ===== GLOBAL DECISION PROCESS =====");
            System.out.println("[" + id + "] Received conflict from " + coordinatorId);
            System.out.println("[" + id + "] Conflict machine: " + affectedMachine + " at site: " + siteId);
            System.out.println("[" + id + "] Analyzing global factory state...");

            String resolution = "APPROVED: " + conflict.get("strategy");

            // Check if another site can take over the work
            System.out.println("[" + id + "] Checking alternative sites...");
            for (SiteCoordinator coordinator : coordinators.values()) {
                String otherSiteId = coordinator.getSiteId();
                if (!otherSiteId.equals(siteId)) {
                    int operational = (int) coordinator.localMachines.values().stream()
                        .filter(m -> m.getState().getStatus() == MachineState.Status.OPERATIONAL)
                        .count();
                    
                    System.out.println("[" + id + "] Site " + otherSiteId + 
                                     " has " + operational + " operational machines");
                    
                    if (operational > 1) {
                        resolution = "GLOBAL_BYPASS: REASSIGN_TO_SITE_" + otherSiteId;
                        System.out.println("[" + id + "] ✓ Solution found: Reassign to " + otherSiteId);
                        break;
                    }
                }
            }

            System.out.println("[" + id + "] Final global decision: " + resolution);
            resolutions.add(resolution);
            return resolution;
        }

        public void step() {
            // Supervisor can monitor overall system health
        }

        public List<String> getResolutions() {
            return new ArrayList<>(resolutions);
        }
    }
}
