import java.util.*;

/**
 * Defines various interaction scenarios between agents in the factory system.
 * Demonstrates different types of agent communication patterns and coordination.
 */
public class InteractionScenarios {

    /**
     * Scenario 1: Simple Failure Detection and Notification
     * Flow: Machine fails → Monitor detects → Monitor notifies RLRA
     */
    public static class Scenario1_SimpleFailureDetection {
        public static void run() {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("SCENARIO 1: Simple Failure Detection and Notification");
            System.out.println("=".repeat(70));

            // Setup
            MachineAgent machine = new MachineAgent("M1_Press", 4);
            MonitorAgent monitor = new MonitorAgent("Monitor_Site_A", "SITE_A", "RLRA_Main");
            monitor.registerMachine("M1_Press", machine.getState());

            System.out.println("\n1. Normal operation (machine operational):");
            System.out.println("   Monitor continuously polls machine state");
            monitor.step();
            System.out.println("   Status: " + machine.getState().getStatus());

            System.out.println("\n2. Machine failure occurs:");
            System.out.println("   Sensor detects abnormality");
            machine.simulateFailure("Pressure sensor reading too high");
            System.out.println("   Machine status: " + machine.getState().getStatus());

            System.out.println("\n3. Monitor detects failure:");
            System.out.println("   Next monitor poll identifies failure state");
            // Simulate monitor detecting the failure and sending alert
            MessageBroker broker = MessageBroker.getInstance();
            Map<String, Object> failureInfo = new HashMap<>();
            failureInfo.put("affectedMachine", "M1_Press");
            failureInfo.put("issue", machine.getState().getErrorMessage());
            failureInfo.put("siteId", "SITE_A");
            failureInfo.put("timestamp", System.currentTimeMillis());

            Message failureNotification = new Message(
                "Monitor_Site_A",
                "RLRA_Main",
                Message.MessageType.RECONFIGURATION_REQUEST,
                failureInfo
            );
            broker.sendMessage(failureNotification);
            System.out.println("   Sent RECONFIGURATION_REQUEST to RLRA");
            System.out.println("   Reason: " + failureInfo.get("issue"));

            System.out.println("\n4. Expected RLRA Response:");
            System.out.println("   - Receive failure notification");
            System.out.println("   - Analyze situation");
            System.out.println("   - Decide on strategy (BYPASS/REASSIGNMENT/ACCELERATION)");
            System.out.println("   - Send action commands to machines");

            System.out.println("\n✓ Scenario 1 completed\n");

            // Cleanup
            machine.stop();
            monitor.stop();
        }
    }

    /**
     * Scenario 2: Multi-Site Coordination
     * Flow: Failure on Site A → Coordinator A decides → Might escalate to Supervisor
     */
    public static class Scenario2_MultiSiteCoordination {
        public static void run() {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("SCENARIO 2: Multi-Site Coordination");
            System.out.println("=".repeat(70));

            System.out.println("\n1. Normal state - Both sites operational:");
            System.out.println("   Site A: M1_Distribution (OPERATIONAL), M2_Machining (OPERATIONAL)");
            System.out.println("   Site B: M3_Assembly (OPERATIONAL), M4_QualityControl (OPERATIONAL)");

            System.out.println("\n2. Failure at Site A:");
            System.out.println("   M2_Machining stops working");
            System.out.println("   Monitor_A detects and sends alert to Coordinator_A");

            System.out.println("\n3. Coordinator_A Decision Process:");
            System.out.println("   - Receives failure notification");
            System.out.println("   - Checks local resources: M1 still operational");
            System.out.println("   - Decision: Can handle locally - bypass M2");
            System.out.println("   - Actions: Route M1 output directly to transport");

            System.out.println("\n4. No escalation needed:");
            System.out.println("   - Local decision sufficient");
            System.out.println("   - Site A maintains production");
            System.out.println("   - No inter-site coordination required");

            System.out.println("\n5. Result:");
            System.out.println("   ✓ M1 continues production");
            System.out.println("   ✓ Parts bypass M2 and go to M3");
            System.out.println("   ✓ Factory continues with degraded capacity");
            System.out.println("   ✓ Coordinator_A logs decision");

            System.out.println("\n✓ Scenario 2 completed\n");
        }
    }

    /**
     * Scenario 3: Conflict Resolution
     * Flow: Local decision conflicts with global constraints → Supervisor resolves
     */
    public static class Scenario3_ConflictResolution {
        public static void run() {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("SCENARIO 3: Conflict Resolution");
            System.out.println("=".repeat(70));

            System.out.println("\n1. Initial state:");
            System.out.println("   Site A: M1 (OPERATIONAL), M2 (OPERATIONAL)");
            System.out.println("   Site B: M3 (OPERATIONAL), M4 (OPERATIONAL)");

            System.out.println("\n2. Both M2 and M1 fail simultaneously:");
            System.out.println("   M1 and M2 experience failures at nearly same time");
            System.out.println("   Coordinator_A receives two failure alerts");

            System.out.println("\n3. Coordinator_A analysis:");
            System.out.println("   - M1 failed: source of parts");
            System.out.println("   - M2 failed: processing of parts");
            System.out.println("   - Both backup solutions on Site A involve same resources");
            System.out.println("   - CONFLICT: No viable local solution");

            System.out.println("\n4. Escalation to Supervisor:");
            System.out.println("   Coordinator_A sends CONFLICT_REPORT to Supervisor:");
            System.out.println("   {");
            System.out.println("     siteId: 'SITE_A',");
            System.out.println("     issue: 'Dual machine failure with no local recovery',");
            System.out.println("     affectedMachines: [M1, M2],");
            System.out.println("     coordinatorId: 'Coordinator_A'");
            System.out.println("   }");

            System.out.println("\n5. Supervisor decision process:");
            System.out.println("   - Analyzes global factory state");
            System.out.println("   - Checks Site B capacity");
            System.out.println("   - M3 and M4 at Site B are operational");
            System.out.println("   - Decision: Redirect transport to bypass Site A");

            System.out.println("\n6. Supervisor sends resolution:");
            System.out.println("   CONFLICT_RESOLUTION: 'BYPASS_SITE_A_REDIRECT_TO_SITE_B'");
            System.out.println("   Instructions:");
            System.out.println("     - All incoming parts go directly to Site B");
            System.out.println("     - Site B M3 will handle both assembly and distribution");
            System.out.println("     - Reduce intake from M1/M2 to zero");

            System.out.println("\n7. Coordinator_A executes resolution:");
            System.out.println("   - Updates local routing tables");
            System.out.println("   - Notifies machines of new configuration");
            System.out.println("   - Monitors for further failures");

            System.out.println("\n8. Result:");
            System.out.println("   ✓ Conflict resolved at global level");
            System.out.println("   ✓ Production continues on Site B");
            System.out.println("   ✓ Site A undergoes maintenance/repair");
            System.out.println("   ✓ System maintains stability");

            System.out.println("\n✓ Scenario 3 completed\n");
        }
    }

    /**
     * Scenario 4: Sequential Dependencies
     * Flow: M1 → M2 → Transport → M3 → M4
     * When M2 fails, coordination ensures M1 production is adjusted
     */
    public static class Scenario4_SequentialDependencies {
        public static void run() {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("SCENARIO 4: Sequential Dependencies Management");
            System.out.println("=".repeat(70));

            System.out.println("\n1. Production flow:");
            System.out.println("   M1 (2s) → M2 (5s) → Transport (4s) → M3 (3s) → M4 (2s)");
            System.out.println("   Bottleneck: M2 (longest cycle)");

            System.out.println("\n2. M2 degrades (cycle time increases to 8s):");
            System.out.println("   M2 now slower than normal");
            System.out.println("   Monitor_A detects: cycle_time > threshold");

            System.out.println("\n3. Cascade effect analysis:");
            System.out.println("   M1: Produces parts faster than M2 can process");
            System.out.println("   → Buffer/queue builds up between M1 and M2");
            System.out.println("   M3: Waits for parts from M2");
            System.out.println("   → M3 becomes idle");
            System.out.println("   M4: Also becomes idle");

            System.out.println("\n4. Coordinator decision:");
            System.out.println("   Strategy: LOAD_BALANCING");
            System.out.println("   Actions:");
            System.out.println("     - Reduce M1 production rate (add delay)");
            System.out.println("     - Accumulate parts in buffer");
            System.out.println("     - Maintain queue length at acceptable level");
            System.out.println("     - Prevent downstream starvation");

            System.out.println("\n5. Message flow:");
            System.out.println("   Monitor_A: STATE_UPDATE (M2 cycle_time=8s)");
            System.out.println("   Coordinator_A: Analyzes dependencies");
            System.out.println("   Coordinator_A → M1: EXECUTE_ACTION (REDUCE_RATE:0.75)");
            System.out.println("   Coordinator_A → M2: EXECUTE_ACTION (PRIORITY:HIGH)");
            System.out.println("   M2: ACTION_RESULT (Processing at reduced rate)");

            System.out.println("\n6. Equilibrium reached:");
            System.out.println("   M1 produces at 75% rate → matches M2 degraded capacity");
            System.out.println("   No queue buildup");
            System.out.println("   M3 continues receiving steady stream");
            System.out.println("   System maintains production flow");

            System.out.println("\n✓ Scenario 4 completed\n");
        }
    }

    /**
     * Scenario 5: Resource Sharing
     * Flow: Multiple requests for same resource require arbitration
     */
    public static class Scenario5_ResourceSharing {
        public static void run() {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("SCENARIO 5: Resource Sharing and Arbitration");
            System.out.println("=".repeat(70));

            System.out.println("\n1. Scenario setup:");
            System.out.println("   M2 can use Tool_A (multi-use tool) for different operations");
            System.out.println("   M3 also needs Tool_A for assembly");
            System.out.println("   Tool_A can only be used by one machine at a time");

            System.out.println("\n2. Conflict emerges:");
            System.out.println("   Time T1: M2 requests Tool_A");
            System.out.println("           Tool_A allocated to M2");
            System.out.println("   Time T2: M3 requests Tool_A");
            System.out.println("           Tool_A busy with M2");

            System.out.println("\n3. Coordinator arbitration:");
            System.out.println("   Receives conflicting requests:");
            System.out.println("   {");
            System.out.println("     requester: [M2, M3],");
            System.out.println("     resource: 'Tool_A',");
            System.out.println("     priority: [NORMAL, NORMAL]");
            System.out.println("   }");

            System.out.println("\n4. Decision criteria:");
            System.out.println("   - Check priority levels (both NORMAL)");
            System.out.println("   - Check queue length for each machine");
            System.out.println("   - M2 queue: 5 parts waiting");
            System.out.println("   - M3 queue: 2 parts waiting");
            System.out.println("   Decision: Grant to M2 (higher demand)");

            System.out.println("\n5. Resolution actions:");
            System.out.println("   - Notify M2: RESOURCE_GRANTED (Tool_A)");
            System.out.println("   - Notify M3: RESOURCE_QUEUED (position=1, wait_time≈3s)");
            System.out.println("   - Set timeout: Release Tool_A after 5 minutes");
            System.out.println("   - Monitor: Track actual usage time");

            System.out.println("\n6. M2 completes work:");
            System.out.println("   Message: RESOURCE_RELEASED (Tool_A)");
            System.out.println("   Coordinator: Check queue - M3 is next");
            System.out.println("   Coordinator → M3: RESOURCE_GRANTED (Tool_A)");

            System.out.println("\n7. Result:");
            System.out.println("   ✓ Resource allocated fairly");
            System.out.println("   ✓ No deadlocks");
            System.out.println("   ✓ Predictable queue management");
            System.out.println("   ✓ Machines informed of status");

            System.out.println("\n✓ Scenario 5 completed\n");
        }
    }

    /**
     * Scenario 6: Cascading Failures
     * Flow: One failure triggers another, requiring intelligent recovery
     */
    public static class Scenario6_CascadingFailures {
        public static void run() {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("SCENARIO 6: Cascading Failures");
            System.out.println("=".repeat(70));

            System.out.println("\n1. Initial failure - Transport system breaks:");
            System.out.println("   Transport (T1) conveyor belt stuck");
            System.out.println("   Status: FAILED");
            System.out.println("   Parts cannot move from Site A to Site B");

            System.out.println("\n2. Primary impact:");
            System.out.println("   M1 and M2 at Site A:");
            System.out.println("     - Continue producing parts");
            System.out.println("     - Queue fills up quickly");
            System.out.println("     - No available space to place new parts");

            System.out.println("\n3. Secondary failure - Storage overflow:");
            System.out.println("   Buffer queue reaches capacity");
            System.out.println("   M2 cannot place completed parts");
            System.out.println("   M2 stalls (mechanical blockage detection)");
            System.out.println("   Status: FAILED (secondary, caused by primary)");

            System.out.println("\n4. Tertiary failure - Upstream starvation:");
            System.out.println("   M1 waits for buffer space");
            System.out.println("   Eventually stalls too");
            System.out.println("   Both M1 and M2 stopped");

            System.out.println("\n5. Coordinator diagnosis:");
            System.out.println("   Receives 3 failure notifications:");
            System.out.println("   - Transport FAILED");
            System.out.println("   - M2 FAILED (secondary)");
            System.out.println("   - M1 FAILED (tertiary)");

            System.out.println("\n6. Root cause analysis:");
            System.out.println("   Coordinator traces failures:");
            System.out.println("   Root cause: Transport failure");
            System.out.println("   Secondary: Buffer overflow");
            System.out.println("   Tertiary: Upstream blockage");

            System.out.println("\n7. Intelligent recovery:");
            System.out.println("   Strategy: DRAIN_AND_RESET");
            System.out.println("   Steps:");
            System.out.println("   1. STOP M1, M2 (prevent further overflow)");
            System.out.println("   2. ACTIVATE_EMERGENCY_BUFFER (divert parts)");
            System.out.println("   3. CLEAR_BLOCKAGE (manual/robotic intervention)");
            System.out.println("   4. REPAIR_TRANSPORT (call maintenance)");

            System.out.println("\n8. Parallel monitoring:");
            System.out.println("   While transport repairs ongoing:");
            System.out.println("   - Monitor M1, M2 status");
            System.out.println("   - Check buffer capacity");
            System.out.println("   - Prepare resumption sequence");

            System.out.println("\n9. Resumption sequence:");
            System.out.println("   Transport REPAIRED → OPERATIONAL");
            System.out.println("   Coordinator enables M1, M2 in sequence:");
            System.out.println("   1. M1: RESET → OPERATIONAL (verify);");
            System.out.println("   2. M2: RESET → OPERATIONAL (verify);");
            System.out.println("   3. Resume normal production flow");

            System.out.println("\n10. Result:");
            System.out.println("   ✓ Cascading failure handled intelligently");
            System.out.println("   ✓ Root cause identified");
            System.out.println("   ✓ Systematic recovery executed");
            System.out.println("   ✓ Damage minimized");

            System.out.println("\n✓ Scenario 6 completed\n");
        }
    }

    /**
     * Run all scenarios
     */
    public static void runAll() {
        System.out.println("\n");
        System.out.println("╔" + "═".repeat(68) + "╗");
        System.out.println("║" + " ".repeat(15) + "INTER-AGENT INTERACTION SCENARIOS" + " ".repeat(21) + "║");
        System.out.println("╚" + "═".repeat(68) + "╝");

        Scenario1_SimpleFailureDetection.run();
        Scenario2_MultiSiteCoordination.run();
        Scenario3_ConflictResolution.run();
        Scenario4_SequentialDependencies.run();
        Scenario5_ResourceSharing.run();
        Scenario6_CascadingFailures.run();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("ALL SCENARIOS COMPLETED");
        System.out.println("=".repeat(70) + "\n");
    }
}
