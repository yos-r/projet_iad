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

        SiteCoordinator coordinator = coordinators.get(siteId);
        if (coordinator != null) {
            coordinator.handleReconfigurationRequest(request);
        }
    }

    private void handleConflictReport(Message message) {
        Map<String, Object> conflict = (Map<String, Object>) message.getPayload();
        String resolution = supervisor.resolveConflict(conflict);
        System.out.println("[" + id + "] Supervisor resolved conflict: " + resolution);

        // Notify relevant coordinators
        String siteId = (String) conflict.get("siteId");
        SiteCoordinator coordinator = coordinators.get(siteId);
        if (coordinator != null) {
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

        public SiteCoordinator(String id, String siteId) {
            super(id, "Coordinator");
            this.siteId = siteId;
            this.localMachines = new HashMap<>();
            this.decisions = new ArrayList<>();
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
            System.out.println("[" + id + "] Local coordinator decision for " + affectedMachine);

            // Make local decision
            String strategy = decideLocalStrategy(affectedMachine);
            decisions.add(strategy);

            // Check for local conflicts
            if (hasLocalConflict(affectedMachine, strategy)) {
                reportConflict(affectedMachine, strategy);
            } else {
                executeLocalDecision(strategy, affectedMachine);
            }
        }

        private void handleConflictResolution(String resolution) {
            System.out.println("[" + id + "] Received conflict resolution: " + resolution);
            // Apply supervisor's decision
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
            Map<String, Object> conflict = new HashMap<>();
            conflict.put("siteId", siteId);
            conflict.put("affectedMachine", affectedMachine);
            conflict.put("strategy", strategy);
            conflict.put("coordinatorId", id);

            // Would send to supervisor in real implementation
            System.out.println("[" + id + "] Reporting conflict to supervisor");
        }

        private void executeLocalDecision(String strategy, String affectedMachine) {
            System.out.println("[" + id + "] Executing local decision: " + strategy);
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

        public String resolveConflict(Map<String, Object> conflict) {
            String affectedMachine = (String) conflict.get("affectedMachine");
            String strategy = (String) conflict.get("strategy");

            System.out.println("[" + id + "] Resolving global conflict for " + affectedMachine);

            // Supervisor logic: check all sites and make global decision
            String resolution = "APPROVED: " + strategy;

            // Check if another site can take over the work
            for (SiteCoordinator coordinator : coordinators.values()) {
                String siteId = coordinator.getSiteId();
                if (!siteId.equals(conflict.get("siteId"))) {
                    // Check if this site has capacity
                    int operational = (int) coordinator.localMachines.values().stream()
                        .filter(m -> m.getState().getStatus() == MachineState.Status.OPERATIONAL)
                        .count();
                    if (operational > 1) {
                        resolution = "REASSIGN_TO_SITE: " + siteId;
                        break;
                    }
                }
            }

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
