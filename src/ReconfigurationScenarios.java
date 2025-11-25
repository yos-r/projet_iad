/**
 * Advanced reconfiguration scenarios beyond basic machine failure handling.
 * Includes production peaks, product changes, and complex system adjustments.
 */
public class ReconfigurationScenarios {

    /**
     * Scenario A: Production Peak (High Demand)
     * Three urgent orders arrive simultaneously, requiring acceleration
     */
    public static class Scenario_ProductionPeak {
        public static void run() {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("SCENARIO A: Production Peak - Handling High Demand");
            System.out.println("=".repeat(70));

            System.out.println("\nContext:");
            System.out.println("  Normal demand: 1-2 orders per 30 minutes");
            System.out.println("  Current state: All machines operational at standard speed");
            System.out.println("  Cycle times: M1=2s, M2=5s, M3=3s, M4=2s");

            System.out.println("\n1. Urgent orders received:");
            System.out.println("  - Order A (100 units, needed in 15 minutes)");
            System.out.println("  - Order B (150 units, needed in 15 minutes)");
            System.out.println("  - Order C (75 units, needed in 20 minutes)");
            System.out.println("  Total: 325 units in peak demand");

            System.out.println("\n2. Impact analysis:");
            System.out.println("  Current capacity: 1 unit per ~12 seconds (M2 bottleneck)");
            System.out.println("  Required throughput: 325 units / 900 seconds = 0.36 units/sec");
            System.out.println("  Current throughput: ~0.083 units/sec");
            System.out.println("  SHORTFALL: Need 4.3x capacity increase");

            System.out.println("\n3. Reconfiguration Strategy: ACCELERATION_MODE");
            System.out.println("  Actions:");
            System.out.println("    a) Reduce M2 cycle time: 5s → 3s (600% faster)");
            System.out.println("       Method: Skip non-critical quality checks");
            System.out.println("       Risk: Slightly lower quality (acceptable for urgent orders)");
            System.out.println("");
            System.out.println("    b) Enable parallel processing on M3: 1 lane → 2 lanes");
            System.out.println("       Method: Run two assembly processes simultaneously");
            System.out.println("       Requirement: Double component supply");
            System.out.println("");
            System.out.println("    c) Adjust M4 quality strategy:");
            System.out.println("       Method: Sample testing (100% → 10% inspection)");
            System.out.println("       Benefit: Reduce time from 2s → 0.5s per unit");
            System.out.println("");
            System.out.println("    d) Pre-buffer management:");
            System.out.println("       Method: Stage M1 output to eliminate waiting");
            System.out.println("       Benefit: Prevent M2 starvation");

            System.out.println("\n4. Message flow - RLRA to machines:");
            System.out.println("  RLRA → M1: EXECUTE_ACTION: ACCELERATE:1.2");
            System.out.println("            (20% speed increase)");
            System.out.println("");
            System.out.println("  RLRA → M2: EXECUTE_ACTION: EMERGENCY_MODE:HIGH_SPEED");
            System.out.println("            (Reduce cycle time by 40%)");
            System.out.println("");
            System.out.println("  RLRA → M3: EXECUTE_ACTION: PARALLEL_MODE:2_LANES");
            System.out.println("            (Enable dual assembly)");
            System.out.println("");
            System.out.println("  RLRA → M4: EXECUTE_ACTION: SAMPLE_TESTING:10_PERCENT");
            System.out.println("            (Reduce inspection overhead)");
            System.out.println("");
            System.out.println("  RLRA → Monitor_A: RECONFIGURATION_PLAN:");
            System.out.println("                    'ACCELERATION_MODE_ACTIVE'");
            System.out.println("                    Expected duration: 30 minutes");

            System.out.println("\n5. Parallel resource preparation:");
            System.out.println("  Component supply chain adjustment:");
            System.out.println("    - Frame supplier: increase supply rate");
            System.out.println("    - Motor supplier: double delivery pace");
            System.out.println("    - Fastener supplier: 8x increase (for parallel M3)");
            System.out.println("  Estimated: 10-minute setup, then steady supply");

            System.out.println("\n6. Monitoring during peak:");
            System.out.println("  Metrics tracked:");
            System.out.println("    - Current output rate (units/minute)");
            System.out.println("    - Component inventory levels");
            System.out.println("    - Machine temperature/stress indicators");
            System.out.println("    - Defect rate vs expected");
            System.out.println("    - Remaining time to deadline");

            System.out.println("\n7. Progress milestones:");
            System.out.println("  Time=5min:  125 units completed (target: 42)  ✓ AHEAD");
            System.out.println("  Time=10min: 250 units completed (target: 83)  ✓ AHEAD");
            System.out.println("  Time=15min: 325 units completed (target: 125) ✓ DEADLINE MET");

            System.out.println("\n8. Recovery to normal mode:");
            System.out.println("  Once peak demand satisfied:");
            System.out.println("    1. Return M2 to normal cycle time");
            System.out.println("    2. Disable M3 parallel mode");
            System.out.println("    3. Return M4 to full inspection");
            System.out.println("    4. Reduce component supply to normal levels");
            System.out.println("    5. Log peak performance metrics");

            System.out.println("\n9. Post-peak analysis:");
            System.out.println("  Actual vs Planned:");
            System.out.println("    - Completed: 325 units (target: 325) ✓");
            System.out.println("    - Time: 15 minutes (planned: 15) ✓");
            System.out.println("    - Quality: 99.2% pass rate (acceptable)");
            System.out.println("    - Machine stress: All within limits");
            System.out.println("    - Cost: Premium pricing offset by volume");

            System.out.println("\n10. Result:");
            System.out.println("   ✓ Peak demand handled successfully");
            System.out.println("   ✓ All deadlines met");
            System.out.println("   ✓ Quality maintained at acceptable levels");
            System.out.println("   ✓ Machines returned to normal safely");
            System.out.println("   ✓ System proved scalability");

            System.out.println("\n✓ Scenario A completed\n");
        }
    }

    /**
     * Scenario B: Product Change (Alpha → Beta)
     * Requires reprogramming machines and recalibration
     */
    public static class Scenario_ProductChange {
        public static void run() {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("SCENARIO B: Product Change - From Alpha to Beta");
            System.out.println("=".repeat(70));

            System.out.println("\nContext:");
            System.out.println("  Current production: Alpha (simple product)");
            System.out.println("  Switching to: Beta (complex product with sensors)");
            System.out.println("  Reason: Market shift to advanced features");

            System.out.println("\n1. Product specifications:");
            System.out.println("  Alpha (Simple):");
            System.out.println("    - Components: Frame, Motor, Housing, 4x Fasteners");
            System.out.println("    - M2 cycle: 5s (basic machining)");
            System.out.println("    - M3 cycle: 3s (simple assembly)");
            System.out.println("    - M4 cycle: 2s (basic QC)");
            System.out.println("    - Total per unit: 12s");
            System.out.println("");
            System.out.println("  Beta (Complex):");
            System.out.println("    - Components: Frame, Motor, Housing, Sensor, Wiring, 8x Fasteners");
            System.out.println("    - M2 cycle: 5s (same machining)");
            System.out.println("    - M3 cycle: 5s (+2s for wiring/sensor integration)");
            System.out.println("    - M4 cycle: 4s (+2s for sensor calibration checks)");
            System.out.println("    - Total per unit: 14s (+16% time)");

            System.out.println("\n2. Pre-change preparation:");
            System.out.println("  Actions:");
            System.out.println("    - Drain production: Empty M1→M2 pipeline");
            System.out.println("    - Complete all Alpha units: Finish current queue");
            System.out.println("    - Pause new orders: Stop accepting Alpha orders");
            System.out.println("    - Stock verification: Verify Beta component availability");
            System.out.println("  Duration: ~5 minutes");

            System.out.println("\n3. Changeover procedure:");
            System.out.println("  Step 1 - Program update (M2):");
            System.out.println("    Current: Alpha machining profile");
            System.out.println("    Update:  Beta machining profile");
            System.out.println("    Changes: Tool selection, speed curves");
            System.out.println("    Duration: 3 minutes");
            System.out.println("");
            System.out.println("  Step 2 - Assembly reconfiguration (M3):");
            System.out.println("    Current: Frame + Motor + Housing assembly");
            System.out.println("    Update:  + Sensor integration + Wiring");
            System.out.println("    Changes: New gripper positions, additional calibration");
            System.out.println("    Duration: 5 minutes");
            System.out.println("");
            System.out.println("  Step 3 - Quality check update (M4):");
            System.out.println("    Current: Basic dimensional checks");
            System.out.println("    Update:  + Sensor functionality test");
            System.out.println("    Changes: New test procedures, increased test time");
            System.out.println("    Duration: 2 minutes");
            System.out.println("");
            System.out.println("  Step 4 - Component supply adjustment:");
            System.out.println("    Current: Standard component feed rate");
            System.out.println("    Update:  New components (sensors, wiring) introduction");
            System.out.println("    Duration: 2 minutes (pre-staged)");

            System.out.println("\n4. Coordination messages:");
            System.out.println("  T=0min:    RLRA → Monitors: PRODUCT_CHANGE_ALERT");
            System.out.println("             Content: 'Switching Alpha→Beta at 8:30 AM'");
            System.out.println("");
            System.out.println("  T=+1min:   RLRA → M2: EXECUTE_ACTION: HALT_AND_UNLOAD");
            System.out.println("             Content: 'Complete Alpha processing, prepare for Beta'");
            System.out.println("");
            System.out.println("  T=+3min:   RLRA → M2: PROGRAM_UPDATE: BETA_PROFILE");
            System.out.println("             Checksum: 0xABC123, Verification: Required");
            System.out.println("");
            System.out.println("  T=+5min:   RLRA → M3: PROGRAM_UPDATE: BETA_ASSEMBLY");
            System.out.println("             New cycle time: 5s (was 3s)");
            System.out.println("");
            System.out.println("  T=+8min:   RLRA → M4: PROGRAM_UPDATE: BETA_QC");
            System.out.println("             New tests: Sensor_Check, Wiring_Test");
            System.out.println("");
            System.out.println("  T=+10min:  RLRA → All: RESUME_PRODUCTION");
            System.out.println("             Content: 'Beta production starting, first batch small'");

            System.out.println("\n5. Verification phase:");
            System.out.println("  Actions:");
            System.out.println("    - M2: Trial run with dummy parts");
            System.out.println("    - M3: Assembly test with real Beta components");
            System.out.println("    - M4: QC test on sample Beta units");
            System.out.println("  Expected time: 10 minutes");
            System.out.println("  Expected result: All systems reporting 'READY'");

            System.out.println("\n6. Ramp-up strategy:");
            System.out.println("  Phase 1 (Min 0-5):  Small batch (5 units)");
            System.out.println("                       Monitor for issues");
            System.out.println("  Phase 2 (Min 5-15): Medium batch (20 units)");
            System.out.println("                       Verify quality, timing");
            System.out.println("  Phase 3 (Min 15+):  Full production");
            System.out.println("                       Normal pace for Beta");

            System.out.println("\n7. Metrics during changeover:");
            System.out.println("  Production paused: 0 units/min (downtime: ~15 min)");
            System.out.println("  Quality issues: 0 during ramp-up phase (acceptable)");
            System.out.println("  First full Beta unit: ~20 minutes from start");

            System.out.println("\n8. Contingency - Rollback");
            System.out.println("  If critical issues discovered:");
            System.out.println("    - Activate ROLLBACK: Return to Alpha");
            System.out.println("    - Restore M2 Alpha profile");
            System.out.println("    - Restore M3 Alpha assembly");
            System.out.println("    - Restore M4 Alpha QC");
            System.out.println("    - Resume Alpha production");
            System.out.println("  Rollback time: ~5 minutes");

            System.out.println("\n9. Post-change stabilization:");
            System.out.println("  Monitor for:");
            System.out.println("    - Cycle time stability");
            System.out.println("    - Quality metrics (target: 99%+ pass rate)");
            System.out.println("    - Component supply consistency");
            System.out.println("    - Machine stress levels");
            System.out.println("  Duration: First 100 units");

            System.out.println("\n10. Result:");
            System.out.println("   ✓ Product switched from Alpha to Beta");
            System.out.println("   ✓ All machines reconfigured");
            System.out.println("   ✓ Quality verified");
            System.out.println("   ✓ Production resumed");
            System.out.println("   ✓ Downtime minimized (~15 min)");

            System.out.println("\n✓ Scenario B completed\n");
        }
    }

    /**
     * Scenario C: Graceful Degradation
     * Handles progressive failures while maintaining production
     */
    public static class Scenario_GracefulDegradation {
        public static void run() {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("SCENARIO C: Graceful Degradation - Progressive Failure Management");
            System.out.println("=".repeat(70));

            System.out.println("\nContext:");
            System.out.println("  M2 experiencing gradual deterioration");
            System.out.println("  Must maintain production while scheduling maintenance");

            System.out.println("\n1. Failure progression:");
            System.out.println("  T=0h:  M2 operating normally (cycle time: 5s)");
            System.out.println("  T=2h:  First warning (vibration detected)");
            System.out.println("           → Cycle time increases to 5.5s (+10%)");
            System.out.println("  T=4h:  Second warning (efficiency dropping)");
            System.out.println("           → Cycle time increases to 6s (+20%)");
            System.out.println("  T=6h:  Critical threshold (accuracy degrading)");
            System.out.println("           → Quality check recommended");
            System.out.println("  T=8h:  Performance critical (only 75% output)");
            System.out.println("           → Maintenance URGENT");
            System.out.println("  T=10h: Complete failure likely");

            System.out.println("\n2. Stage 1 - Early Warning (T=2h):");
            System.out.println("  Monitor detection:");
            System.out.println("    MESSAGE: STATE_UPDATE");
            System.out.println("    Status: 'Vibration detected, cycle_time increasing'");
            System.out.println("");
            System.out.println("  RLRA response:");
            System.out.println("    - Alert supervisor (informational)");
            System.out.println("    - Schedule preventive maintenance");
            System.out.println("    - No production changes yet");
            System.out.println("");
            System.out.println("  Action: Schedule maintenance window tomorrow evening");

            System.out.println("\n3. Stage 2 - Degradation Detected (T=4h):");
            System.out.println("  Monitor detection:");
            System.out.println("    MESSAGE: STATE_UPDATE");
            System.out.println("    Status: 'Cycle time degraded to 6s, efficiency 80%'");
            System.out.println("");
            System.out.println("  RLRA response:");
            System.out.println("    - Implement load redistribution");
            System.out.println("    - Send ACCELERATE to M1 (+20% speed)");
            System.out.println("    - Send REDUCE_LOAD to M3 (pre-buffer)");
            System.out.println("    - Add buffer capacity (Stage more parts)");
            System.out.println("");
            System.out.println("  Result: Maintains throughput despite M2 degradation");

            System.out.println("\n4. Stage 3 - Critical Threshold (T=6h):");
            System.out.println("  Monitor detection:");
            System.out.println("    MESSAGE: RECONFIGURATION_REQUEST");
            System.out.println("    Status: 'Critical degradation, accuracy at 85%'");
            System.out.println("");
            System.out.println("  RLRA decision:");
            System.out.println("    - Activate EMERGENCY_MAINTENANCE_MODE");
            System.out.println("    - Notify all machines of degraded output");
            System.out.println("    - M4 increases quality sampling to 50% (detect issues)");
            System.out.println("    - Prepare M2_backup (if available) for switchover");
            System.out.println("");
            System.out.println("  Actions:");
            System.out.println("    RLRA → M2: REDUCE_SPEED: 0.9 (extend cycle to 6.7s)");
            System.out.println("              Reason: Reduce mechanical stress");
            System.out.println("    RLRA → M1: ACCELERATE: 1.3 (increase output)");
            System.out.println("              Reason: Compensate for M2 slowdown");
            System.out.println("    RLRA → M4: QUALITY_MODE: ENHANCED");
            System.out.println("              Reason: Catch defects from M2");

            System.out.println("\n5. Stage 4 - Imminent Failure (T=8h):");
            System.out.println("  Monitor detection:");
            System.out.println("    MESSAGE: RECONFIGURATION_REQUEST URGENT");
            System.out.println("    Status: 'M2 at 75% capacity, failure expected within 2h'");
            System.out.println("");
            System.out.println("  RLRA decision:");
            System.out.println("    - Initiate BYPASS_PREPARATION");
            System.out.println("    - Reduce intake: Stop new orders to M2");
            System.out.println("    - Extend buffer: Queue parts before M2");
            System.out.println("    - Reroute planning: If M2 fails, parts go to M3 directly");
            System.out.println("    - Alert maintenance: Deploy immediately");
            System.out.println("");
            System.out.println("  Actions:");
            System.out.println("    RLRA → M1: QUEUE_MODE: EXTENDED_BUFFER");
            System.out.println("              (Can store up to 100 parts)");
            System.out.println("    RLRA → M2: MONITOR_ONLY: (No processing)");
            System.out.println("              (Let maintenance access machine)");
            System.out.println("    RLRA → Maintenance: SERVICE_URGENT: M2");
            System.out.println("              (Start immediate repair)");

            System.out.println("\n6. Failure occurs (T=9h):");
            System.out.println("  M2 enters FAILED state");
            System.out.println("  RLRA response is immediate (already prepared):");
            System.out.println("    - Activate bypass (parts M1→M3)");
            System.out.println("    - Reduce intake to match reduced capacity");
            System.out.println("    - Continue production with degraded output");
            System.out.println("    - No dramatic reconfiguration needed");

            System.out.println("\n7. Maintenance and recovery:");
            System.out.println("  M2 repaired and tested (est. 2 hours)");
            System.out.println("  M2 returned to production");
            System.out.println("  Resume normal configuration");

            System.out.println("\n8. Comparison: Graceful vs Abrupt Failure");
            System.out.println("  Graceful degradation:");
            System.out.println("    - 8 hours advance warning");
            System.out.println("    - Planned maintenance window");
            System.out.println("    - Smooth production adjustment");
            System.out.println("    - Minimal quality impact");
            System.out.println("    - Cost: Reduced efficiency + prevention");
            System.out.println("");
            System.out.println("  Abrupt failure (no early detection):");
            System.out.println("    - Sudden stoppage");
            System.out.println("    - Emergency rerouting");
            System.out.println("    - Potential queue overflow");
            System.out.println("    - Possible downstream stalls");
            System.out.println("    - Cost: Downtime + lost production");

            System.out.println("\n9. Result:");
            System.out.println("   ✓ Failure predicted 8 hours in advance");
            System.out.println("   ✓ Production continued throughout");
            System.out.println("   ✓ Maintenance executed at planned time");
            System.out.println("   ✓ Minimal disruption to customers");
            System.out.println("   ✓ Reduced overall costs");

            System.out.println("\n✓ Scenario C completed\n");
        }
    }

    /**
     * Scenario D: Component Supply Chain Failure
     * Handles shortage of critical components
     */
    public static class Scenario_SupplyChainFailure {
        public static void run() {
            System.out.println("\n" + "=".repeat(70));
            System.out.println("SCENARIO D: Component Supply Chain Failure");
            System.out.println("=".repeat(70));

            System.out.println("\nContext:");
            System.out.println("  M3 (Assembly) requires multiple components");
            System.out.println("  Supplier disruption: Motor supplier unable to deliver");

            System.out.println("\n1. Normal component supply:");
            System.out.println("  Frame:     1 unit/min from Supplier_A");
            System.out.println("  Motor:     1 unit/min from Supplier_B (PRIMARY)");
            System.out.println("  Housing:   1 unit/min from Supplier_C");
            System.out.println("  Fasteners: 4 units/min from Supplier_D");
            System.out.println("  Buffer:    5-unit safety stock for motors");

            System.out.println("\n2. Supplier alert (T=0):");
            System.out.println("  Supplier_B notification:");
            System.out.println("    'Unexpected equipment failure, no motors for 4 hours'");

            System.out.println("\n3. M3 component status:");
            System.out.println("  Current inventory:");
            System.out.println("    Frames:    12 units");
            System.out.println("    Motors:    4 units (buffer)");
            System.out.println("    Housing:   15 units");
            System.out.println("    Fasteners: 50 units");

            System.out.println("\n4. Problem analysis:");
            System.out.println("  M3 cycle time: 3s/unit");
            System.out.println("  Motor consumption: 1 unit / 3s = 0.33 units/sec");
            System.out.println("  Current buffer: 4 motors");
            System.out.println("  Time until stockout: 4 motors × 3s = 12s (minimal!)");

            System.out.println("\n5. Immediate response (RLRA Decision):");
            System.out.println("  Option A - Stop production (simplest):");
            System.out.println("    Risk: Upstream queues overflow, cascade failures");
            System.out.println("");
            System.out.println("  Option B - Use alternate supplier:");
            System.out.println("    Supplier_E (premium, higher cost):");
            System.out.println("    - Cost: 50% more expensive");
            System.out.println("    - Delivery: 1 hour lead time");
            System.out.println("    - Quantity: Up to 20 units available");
            System.out.println("    Decision: ACTIVATE emergency supplier");
            System.out.println("");
            System.out.println("  Option C - Reduce production capacity temporarily:");
            System.out.println("    - Decrease M3 output by 50%");
            System.out.println("    - Extend buffer to stretch existing motors");
            System.out.println("    - Combined with Option B");

            System.out.println("\n6. Execution plan:");
            System.out.println("  Action 1 - Emergency order (T=0):");
            System.out.println("    Contact Supplier_E for 20 emergency motors");
            System.out.println("    Expected delivery: T=1 hour");
            System.out.println("    Expedited shipping: +$500");
            System.out.println("");
            System.out.println("  Action 2 - Production adjustment (T=0):");
            System.out.println("    RLRA → M3: EXECUTE_ACTION: REDUCE_RATE:0.5");
            System.out.println("              (50% slower, less motor consumption)");
            System.out.println("    RLRA → M1: QUEUE_MODE: EXTEND_BUFFER");
            System.out.println("              (Upstream can wait)");
            System.out.println("    RLRA → M4: HOLD: (Downstream empty)");
            System.out.println("              (Wait for assembly output)");
            System.out.println("");
            System.out.println("  Action 3 - Monitoring (Continuous):");
            System.out.println("    Track motor inventory");
            System.out.println("    Estimate runway: 4 motors ÷ 0.16 units/sec = 25 seconds");
            System.out.println("    With 50% reduction: 4 motors ÷ 0.08 units/sec = 50 seconds");

            System.out.println("\n7. Timeline:");
            System.out.println("  T=0min:   Issue detected, emergency order placed");
            System.out.println("            M3 reduces to 50% production");
            System.out.println("  T=30min:  Motors still available, production stable");
            System.out.println("  T=60min:  Emergency motors arrive from Supplier_E");
            System.out.println("  T=65min:  M3 returns to full production");
            System.out.println("  T=120min: Supplier_B back online with resumed deliveries");
            System.out.println("  T=300min: Supplier_B fully caught up, cost absorbed");

            System.out.println("\n8. Cost analysis:");
            System.out.println("  Emergency supply (20 units @ 50% premium): +$500");
            System.out.println("  Production loss (50% for 1 hour): ~$200 lost revenue");
            System.out.println("  Total cost: ~$700");
            System.out.println("  vs Shutdown (4 hours): ~$5,000 lost revenue");
            System.out.println("  Savings: $4,300");

            System.out.println("\n9. Alternative - If no emergency supplier available:");
            System.out.println("  Actions:");
            System.out.println("    1. STOP M3 (no motors)");
            System.out.println("    2. QUEUE M1,M2 output (build buffer)");
            System.out.println("    3. DRAIN M4 (process queued work)");
            System.out.println("    4. WAIT for Supplier_B recovery (4 hours)");
            System.out.println("    5. RAMP-UP M3 when motors available");
            System.out.println("    Result: 4-hour downtime, $5,000 loss");

            System.out.println("\n10. Result:");
            System.out.println("   ✓ Supply chain failure handled");
            System.out.println("   ✓ Production maintained at 50%");
            System.out.println("   ✓ Emergency supplier activated");
            System.out.println("   ✓ Minimal financial impact");
            System.out.println("   ✓ Customer orders partially met");

            System.out.println("\n✓ Scenario D completed\n");
        }
    }

    /**
     * Run all scenarios
     */
    public static void runAll() {
        System.out.println("\n");
        System.out.println("╔" + "═".repeat(68) + "╗");
        System.out.println("║" + " ".repeat(10) + "ADVANCED RECONFIGURATION SCENARIOS" + " ".repeat(24) + "║");
        System.out.println("╚" + "═".repeat(68) + "╝");

        Scenario_ProductionPeak.run();
        Scenario_ProductChange.run();
        Scenario_GracefulDegradation.run();
        Scenario_SupplyChainFailure.run();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("ALL ADVANCED SCENARIOS COMPLETED");
        System.out.println("=".repeat(70) + "\n");
    }
}
