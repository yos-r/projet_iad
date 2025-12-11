import java.util.*;

/**
 * Simulation framework for testing different RLRA architectures.
 */
public class FactorySimulation {
    private BaseAgent rlra;
    private RLRAFactory.ArchitectureType architectureType;
    private Map<String, MachineAgent> machines;
    private Map<String, MonitorAgent> monitors;
    private TransportAgent transport;
    private int simulationSteps;
    private MessageBroker messageBroker;

    public FactorySimulation(RLRAFactory.ArchitectureType type) {
        this.architectureType = type;
        this.rlra = RLRAFactory.createRLRA(type, "RLRA_Main");
        this.machines = new HashMap<>();
        this.monitors = new HashMap<>();
        this.messageBroker = MessageBroker.getInstance();
        this.simulationSteps = 0;
    }

    /**
     * Initialize factory with machines and monitors.
     */
    public void initializeFactory() {
        // Create machines for Site A
        MachineAgent m1 = createMachine("M1_Distribution", 2);
        MachineAgent m2 = createMachine("M2_Machining", 5);

        // Create machines for Site B
        MachineAgent m3 = createMachine("M3_Assembly", 3);
        MachineAgent m4 = createMachine("M4_QualityControl", 2);

        // Create transport agent between sites
        transport = new TransportAgent("T1_Transport", "SITE_A", "SITE_B", 4, 20);
        System.out.println("Created transport: T1_Transport (buffer capacity: 20 parts)");

        // Create monitors
        MonitorAgent monitorA = createMonitor("Monitor_SiteA", "SITE_A");
        MonitorAgent monitorB = createMonitor("Monitor_SiteB", "SITE_B");

        // Register machines with monitors
        monitorA.registerMachine("M1_Distribution", m1.getState());
        monitorA.registerMachine("M2_Machining", m2.getState());
        monitorB.registerMachine("M3_Assembly", m3.getState());
        monitorB.registerMachine("M4_QualityControl", m4.getState());

        // Register agents with RLRA based on architecture type
        registerAgentsWithRLRA(monitorA, monitorB, m1, m2, m3, m4);

        System.out.println("Factory initialized with " + architectureType.name() + " RLRA architecture");
    }

    private MachineAgent createMachine(String id, int cycleTime) {
        MachineAgent machine = new MachineAgent(id, cycleTime);
        machines.put(id, machine);
        System.out.println("Created machine: " + id + " (cycle time: " + cycleTime + "s)");
        return machine;
    }

    private MonitorAgent createMonitor(String id, String siteId) {
        MonitorAgent monitor = new MonitorAgent(id, siteId, "RLRA_Main");
        monitors.put(id, monitor);
        System.out.println("Created monitor: " + id + " for " + siteId);
        return monitor;
    }

    private void registerAgentsWithRLRA(MonitorAgent monitorA, MonitorAgent monitorB,
                                       MachineAgent m1, MachineAgent m2, MachineAgent m3, MachineAgent m4) {
        if (rlra instanceof RLRACentralized) {
            RLRACentralized centralized = (RLRACentralized) rlra;
            centralized.registerMonitor(monitorA);
            centralized.registerMonitor(monitorB);
            centralized.registerMachine(m1);
            centralized.registerMachine(m2);
            centralized.registerMachine(m3);
            centralized.registerMachine(m4);
        } else if (rlra instanceof RLRAModular) {
            RLRAModular modular = (RLRAModular) rlra;
            modular.registerMonitor(monitorA);
            modular.registerMonitor(monitorB);
            modular.registerMachine(m1);
            modular.registerMachine(m2);
            modular.registerMachine(m3);
            modular.registerMachine(m4);
        } else if (rlra instanceof RLRADistributed) {
            RLRADistributed distributed = (RLRADistributed) rlra;
            RLRADistributed.SiteCoordinator coordA = distributed.createSiteCoordinator("SITE_A");
            RLRADistributed.SiteCoordinator coordB = distributed.createSiteCoordinator("SITE_B");
            coordA.registerMachine(m1);
            coordA.registerMachine(m2);
            coordB.registerMachine(m3);
            coordB.registerMachine(m4);
        }
    }

    /**
     * Simulate a machine failure scenario.
     */
    public void simulateMachineFailure(String machineId, String errorMessage) {
        System.out.println("\n=== SIMULATING FAILURE ===");
        System.out.println("Machine " + machineId + " failed: " + errorMessage);

        MachineAgent machine = machines.get(machineId);
        if (machine != null) {
            machine.simulateFailure(errorMessage);

            // Find which monitor is responsible for this machine
            for (MonitorAgent monitor : monitors.values()) {
                if (monitor.getMonitoredMachines().containsKey(machineId)) {
                    // Trigger reconfiguration request
                    Map<String, Object> failureInfo = new HashMap<>();
                    failureInfo.put("affectedMachine", machineId);
                    failureInfo.put("issue", "Machine failure: " + errorMessage);
                    failureInfo.put("siteId", monitor.getId().contains("A") ? "SITE_A" : "SITE_B");
                    failureInfo.put("affectedMachines", new ArrayList<>(monitor.getMonitoredMachines().keySet()));

                    messageBroker.sendMessage(
                        new Message(monitor.getId(), rlra.getId(),
                                   Message.MessageType.RECONFIGURATION_REQUEST, failureInfo)
                    );
                    break;
                }
            }
        }
    }

    /**
     * Run the simulation for a number of steps.
     */
    public void run(int steps) {
        System.out.println("\n=== STARTING SIMULATION (" + steps + " steps) ===");
        for (int i = 0; i < steps; i++) {
            System.out.println("\n--- Step " + (i + 1) + " ---");
            simulationSteps++;

            // Each agent processes messages
            rlra.step();
            for (MonitorAgent monitor : monitors.values()) {
                monitor.step();
            }
            for (MachineAgent machine : machines.values()) {
                machine.step();
            }

            try {
                Thread.sleep(100); // Small delay between steps
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Print simulation report.
     */
    public void printReport() {
        System.out.println("\n=== SIMULATION REPORT ===");
        System.out.println("Architecture: " + architectureType.name());
        System.out.println("Total simulation steps: " + simulationSteps);
        System.out.println("\nMachine States:");
        for (MachineAgent machine : machines.values()) {
            MachineState state = machine.getState();
            System.out.println("  " + state.getMachineId() + ": " + state.getStatus() +
                             (state.getErrorMessage() != null ? " (" + state.getErrorMessage() + ")" : ""));
        }

        System.out.println("\nReconfiguration History:");
        if (rlra instanceof RLRACentralized) {
            ((RLRACentralized) rlra).getReconfigurationHistory().forEach(h -> System.out.println("  - " + h));
        } else if (rlra instanceof RLRAModular) {
            ((RLRAModular) rlra).getExecutionHistory().forEach(h -> System.out.println("  - " + h));
        } else if (rlra instanceof RLRADistributed) {
            System.out.println("  [Distributed decisions made at coordinator level]");
            ((RLRADistributed) rlra).getSupervisor().getResolutions()
                .forEach(r -> System.out.println("  - " + r));
        }

        messageBroker.printStatistics();
    }

    /**
     * Clean up and stop the simulation.
     */
    public void stop() {
        rlra.stop();
        for (MonitorAgent monitor : monitors.values()) {
            monitor.stop();
        }
        for (MachineAgent machine : machines.values()) {
            machine.stop();
        }
        messageBroker.stop();
    }

    public TransportAgent getTransport() {
        return transport;
    }

    public Map<String, MachineAgent> getMachines() {
        return machines;
    }

    public BaseAgent getRLRA() {
        return rlra;
    }
}
