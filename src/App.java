/**
 * Main application demonstrating three RLRA architectures for factory reconfiguration.
 *
 * This application simulates a FESTO CP Factory with a multi-agent system that handles
 * dynamic reconfiguration in response to machine failures, production peaks, and other anomalies.
 *
 * Features demonstrated:
 * 1. Three RLRA architectures (Centralized, Modular, Distributed)
 * 2. Custom agents (ConveyorAgent, AssemblyAgent)
 * 3. Inter-agent interaction scenarios (6 scenarios)
 * 4. Advanced reconfiguration scenarios (4 scenarios)
 * 5. Communication protocol and conflict resolution
 */
public class App {
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            String mode = args[0].toLowerCase();
            switch (mode) {
                case "arch":
                    demonstrateArchitectures();
                    break;
                case "interactions":
                    InteractionScenarios.runAll();
                    break;
                case "scenarios":
                    ReconfigurationScenarios.runAll();
                    break;
                case "all":
                    runFullDemo();
                    break;
                case "help":
                    printHelp();
                    break;
                default:
                    System.out.println("Unknown mode: " + mode);
                    printHelp();
            }
        } else {
            // Default: run full demo
            runFullDemo();
        }
    }

    /**
     * Run full demonstration of all features
     */
    private static void runFullDemo() {
        System.out.println("╔" + "═".repeat(50) + "╗");
        System.out.println("║" + " ".repeat(8) + "FESTO CP Factory - Complete Demo" + " ".repeat(9) + "║");
        System.out.println("╚" + "═".repeat(50) + "╝\n");

        System.out.println("1. Running RLRA Architecture Demonstrations\n");
        demonstrateArchitectures();

        System.out.println("\n\n2. Running Inter-Agent Interaction Scenarios\n");
        InteractionScenarios.runAll();

        System.out.println("\n3. Running Advanced Reconfiguration Scenarios\n");
        ReconfigurationScenarios.runAll();

        System.out.println("\n" + "═".repeat(70));
        System.out.println("COMPLETE DEMONSTRATION FINISHED");
        System.out.println("═".repeat(70) + "\n");
    }

    /**
     * Demonstrate all three architectures
     */
    private static void demonstrateArchitectures() {
        demonstrateCentralizedArchitecture();
        MessageBroker.getInstance().restart();  // Restart broker between architectures
        System.out.println("\n" + "=".repeat(50) + "\n");
        demonstrateModularArchitecture();
        MessageBroker.getInstance().restart();  // Restart broker between architectures
        System.out.println("\n" + "=".repeat(50) + "\n");
        demonstrateDistributedArchitecture();
    }

    /**
     * Demonstrates the Centralized RLRA architecture.
     */
    private static void demonstrateCentralizedArchitecture() {
        System.out.println("1. CENTRALIZED RLRA ARCHITECTURE");
        System.out.println("-".repeat(40));

        FactorySimulation simulation = new FactorySimulation(RLRAFactory.ArchitectureType.CENTRALIZED);
        simulation.initializeFactory();

        // Run simulation
        simulation.run(2);

        // Simulate a machine failure
        System.out.println("\n[SCENARIO] Running MACHINE_FAILURE scenario using CENTRALIZED RLRA");
        System.out.println("[SCENARIO] Simulating Spindle motor failure on M2_Machining");
        simulation.simulateMachineFailure("M2_Machining", "Spindle motor failure");

        // Continue simulation
        simulation.run(3);

        // Print results
        simulation.printReport();
        simulation.stop();
    }

    /**
     * Demonstrates the Modular (Composite) RLRA architecture.
     */
    private static void demonstrateModularArchitecture() {
        System.out.println("2. MODULAR (COMPOSITE) RLRA ARCHITECTURE");
        System.out.println("-".repeat(40));

        FactorySimulation simulation = new FactorySimulation(RLRAFactory.ArchitectureType.MODULAR);
        simulation.initializeFactory();

        // Run simulation
        simulation.run(2);

        // Simulate a machine failure
        System.out.println("\n[SCENARIO] Running MACHINE_FAILURE scenario using MODULAR RLRA");
        System.out.println("[SCENARIO] Simulating Gripper malfunction on M3_Assembly");
        simulation.simulateMachineFailure("M3_Assembly", "Gripper malfunction");

        // Continue simulation
        simulation.run(3);

        // Print results
        simulation.printReport();
        simulation.stop();
    }

    /**
     * Demonstrates the Distributed RLRA architecture.
     */
    private static void demonstrateDistributedArchitecture() {
        System.out.println("3. DISTRIBUTED RLRA ARCHITECTURE");
        System.out.println("-".repeat(40));

        FactorySimulation simulation = new FactorySimulation(RLRAFactory.ArchitectureType.DISTRIBUTED);
        simulation.initializeFactory();

        // Run simulation
        simulation.run(2);

        // Simulate a machine failure
        System.out.println("\n[SCENARIO] Running MACHINE_FAILURE scenario using DISTRIBUTED RLRA");
        System.out.println("[SCENARIO] Simulating Conveyor belt failure on M1_Distribution");
        System.out.println("[SCENARIO] Testing multi-site coordination and conflict resolution");
        simulation.simulateMachineFailure("M1_Distribution", "Conveyor belt stuck");

        // Continue simulation
        simulation.run(3);

        // Print results
        simulation.printReport();
        simulation.stop();
    }

    /**
     * Print usage information
     */
    private static void printHelp() {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("FESTO CP Factory - Multi-Agent Reconfiguration System");
        System.out.println("═".repeat(70));
        System.out.println("\nUsage: java App [mode]\n");
        System.out.println("Modes:");
        System.out.println("  (none/all)     Run complete demonstration (default)");
        System.out.println("  arch           Run RLRA architecture demonstrations only");
        System.out.println("  interactions   Run inter-agent interaction scenarios");
        System.out.println("  scenarios      Run advanced reconfiguration scenarios");
        System.out.println("  help           Display this help message");
        System.out.println("\nExamples:");
        System.out.println("  java App                  # Full demo");
        System.out.println("  java App arch             # Architectures only");
        System.out.println("  java App interactions     # Interaction scenarios");
        System.out.println("  java App scenarios        # Reconfiguration scenarios");
        System.out.println("\n" + "═".repeat(70) + "\n");
    }
}
