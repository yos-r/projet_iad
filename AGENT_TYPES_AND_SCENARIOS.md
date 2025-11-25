# Agent Types, Interaction Scenarios, and Reconfiguration Scenarios

## Table of Contents
1. [Custom Agent Types](#custom-agent-types)
2. [Inter-Agent Interaction Scenarios](#inter-agent-interaction-scenarios)
3. [Advanced Reconfiguration Scenarios](#advanced-reconfiguration-scenarios)
4. [Running the Demonstrations](#running-the-demonstrations)

---

## Custom Agent Types

### 1. ConveyorAgent

**File**: `src/ConveyorAgent.java`

**Purpose**: Represents a conveyor belt/transport system that moves parts between factory locations

**Characteristics**:
- Connects two locations (source, destination)
- Carries limited quantity of parts
- Subject to jams and failures
- Can be commanded to speed up, slow down, or stop
- Tracks capacity and throughput

**Key States**:
```java
enum Status {
    MOVING,          // Operating normally
    STOPPED,         // Paused by command
    JAMMED,          // Parts blocked, needs intervention
    FAILED,          // Complete failure, repairs needed
    MAINTENANCE      // Under maintenance
}
```

**Properties**:
```
- transportTime: milliseconds to move parts across conveyor
- maxCapacity: maximum parts that can be on conveyor
- speed: 0.0 to 1.0 (affects transport time)
- status: current operational state
- partsTransported: total count since start
- uptime: time since last failure
```

**Supported Actions**:
```
STOP             - Pause conveyor
START            - Resume conveyor
REDUCE_SPEED:0.8 - Slow to 80% speed
CLEAR_JAM        - Clear jammed parts
EMERGENCY_STOP   - Immediate halt
```

**Failure Modes**:
```
- Jam: Parts pile up, blockage
- Failure: Motor stop, belt break
- Degradation: Slowdown over time
```

**Example Usage**:
```java
ConveyorAgent transport = new ConveyorAgent(
    "Transport_T1",
    "SITE_A",
    "SITE_B",
    4000,        // 4 second transport time
    50           // Capacity: 50 parts
);

// Add parts
transport.addPart("Part_001");
transport.addPart("Part_002");

// Get status
System.out.println("Capacity: " + transport.getCapacityPercentage() + "%");

// Simulate failure
transport.simulateJam("Parts stuck at transfer point");

// Receive command
Message action = new Message("RLRA", "Transport_T1",
    Message.MessageType.EXECUTE_ACTION, "CLEAR_JAM");
transport.receiveMessage(action);
```

---

### 2. AssemblyAgent

**File**: `src/AssemblyAgent.java`

**Purpose**: Represents an advanced assembly machine that combines multiple components into products

**Characteristics**:
- Manages component inventory
- Performs complex assemblies with multiple parts
- Supports different product types with varying requirements
- Requires recalibration when switching products
- Tracks assembly history and completion

**Component Types**:
```java
enum ComponentType {
    FRAME,      // Structural component
    MOTOR,      // Power source
    HOUSING,    // Protective casing
    FASTENER,   // Bolts, screws, etc.
    SENSOR,     // Detection devices
    WIRING      // Electrical connections
}
```

**Product Specifications**:
```
Alpha (Simple):
  - Frame (1), Motor (1), Housing (1), Fastener (4)
  - Cycle time: 3s
  - Complexity: Low

Beta (Complex):
  - Frame (1), Motor (1), Housing (1), Sensor (1), Wiring (1), Fastener (8)
  - Cycle time: 5s
  - Complexity: Medium
  - Requires: Wiring integration, sensor setup

Gamma (Heavy):
  - Frame (2), Motor (1), Housing (1), Sensor (2), Wiring (1), Fastener (12)
  - Cycle time: 7s
  - Complexity: High
  - Requires: Dual frame handling, multiple sensors
```

**Key Features**:
1. **Component Management**: Track inventory of each component type
2. **Product Change**: Switch between products with recalibration
3. **Conditional Assembly**: Can only assemble when all components available
4. **Recalibration**: Time penalty when changing products
5. **Degradation**: Can simulate equipment wear

**Supported Actions**:
```
SUPPLY_COMPONENT:MOTOR:10      - Add 10 motors
PERFORM_ASSEMBLY               - Create one unit
CHANGE_PRODUCT:Beta            - Switch to Beta
RECALIBRATE                     - Manual recalibration
RESET                           - Return to initial state
```

**Assembly Readiness**:
```java
Map<String, Object> status = assembly.getAssemblyStatus();
// Returns:
// {
//   product: "Beta",
//   canAssemble: true,
//   cycleTime: 5,
//   components: {
//     FRAME: "1/1",
//     MOTOR: "1/1",
//     HOUSING: "1/1",
//     SENSOR: "0/1",
//     WIRING: "0/1",
//     FASTENER: "2/8"
//   }
// }
```

**Example Usage**:
```java
AssemblyAgent assembly = new AssemblyAgent("M3_Assembly", 3, "Alpha");

// Supply components for Alpha
assembly.supplyComponent(ComponentType.FRAME, 10);
assembly.supplyComponent(ComponentType.MOTOR, 10);
assembly.supplyComponent(ComponentType.HOUSING, 10);
assembly.supplyComponent(ComponentType.FASTENER, 40);

// Perform assembly
if (assembly.canAssemble()) {
    String partId = assembly.performAssembly();
    System.out.println("Assembled: " + partId);
}

// Change to Beta product
assembly.changeProduct("Beta");
assembly.supplyComponent(ComponentType.SENSOR, 5);
assembly.supplyComponent(ComponentType.WIRING, 10);

// Recalibration happens automatically
String partId = assembly.performAssembly();
```

---

## Inter-Agent Interaction Scenarios

### Overview
Six interaction scenarios demonstrate how agents communicate and coordinate:

### Scenario 1: Simple Failure Detection and Notification

**Duration**: ~200 milliseconds from failure to recovery

**Flow**:
1. Machine fails (error detected)
2. Monitor polls state and detects failure
3. Monitor sends RECONFIGURATION_REQUEST to RLRA
4. RLRA analyzes and decides strategy
5. RLRA sends RECONFIGURATION_PLAN to monitors
6. Monitors and machines execute new configuration

**Messages**:
```
M2_Machining: (Status changes to FAILED)
         ↓
Monitor_SiteA: STATE_UPDATE (failure detected)
         ↓
RLRA_Main: RECONFIGURATION_REQUEST
         ↓
RLRA_Main: Analyzes (30-50ms)
         ↓
RLRA_Main: RECONFIGURATION_PLAN
         ↓
Machines: EXECUTE_ACTION (bypass/reassign)
         ↓
System: Stabilized in new configuration
```

**Key Insight**: Single point of failure handled without cascade

### Scenario 2: Multi-Site Coordination

**Duration**: ~150 milliseconds

**Situation**: M2 fails at Site A, but Site A can handle it locally

**Flow**:
1. Monitor_A detects M2 failure
2. Coordinator_A analyzes local resources
3. Coordinator_A finds M1 still operational
4. Coordinator_A decides: bypass M2, route M1 to transport
5. NO escalation to supervisor needed

**Decision Logic**:
```
if (failedMachine.canBeByPassed()) {
    if (upstreamMachine.canCompensate()) {
        return LOCAL_DECISION;  // Fast path (~100ms)
    }
}
return ESCALATE_TO_SUPERVISOR;  // Slower path (~300ms)
```

**Key Insight**: Local decisions are faster, global decisions handle complex conflicts

### Scenario 3: Conflict Resolution

**Duration**: ~300 milliseconds with escalation

**Situation**: Both M1 and M2 fail simultaneously - no local recovery possible

**Escalation Path**:
1. Monitor_A detects both failures
2. Coordinator_A analyzes: no resources available
3. Coordinator_A → Supervisor: CONFLICT_REPORT
4. Supervisor checks Site B: M3, M4 operational
5. Supervisor → Coordinator_A: CONFLICT_RESOLUTION
6. Coordinator_A executes global decision
7. Production continues with rerouted flow

**Conflict Detection**:
```java
if (numFailedMachines > numBackups) {
    return CONFLICT;
}
if (bufferCapacity < incomingRate * delayTime) {
    return CONFLICT;
}
if (criticalResourceUnavailable()) {
    return CONFLICT;
}
```

**Key Insight**: Escalation prevents deadlocks and ensures system stability

### Scenario 4: Sequential Dependencies Management

**Duration**: Continuous adjustment, ~5 minutes to stabilize

**Situation**: M2 degrades, slowing bottleneck, causing upstream buildup

**Problem Analysis**:
```
Flow: M1(2s) → M2(5s→8s) → M3(3s) → M4(2s)
Impact:
  - M1 produces faster than M2 can process
  - Queue builds: M1 output → buffer
  - M3 waits: No input from buffer
  - Efficiency drops
```

**Solution**:
1. Detect M2 degradation
2. Reduce M1 production rate
3. Extend buffer capacity
4. Maintain queue at equilibrium
5. Prevent downstream starvation

**Flow Adjustment**:
```
M1: Normal rate → 75% rate (matches M2 degraded)
Buffer: 10 parts → 50 parts (accumulate)
M3: Wait time increases, but steady input
```

**Key Insight**: Degradation needs distributed adjustment, not just local fix

### Scenario 5: Resource Sharing and Arbitration

**Duration**: ~1-5 seconds per arbitration decision

**Situation**: Multiple machines request same limited resource

**Example**:
- M2 and M3 both need Tool_A
- Tool_A can only be used by one machine
- Both have work queued

**Arbitration Rules**:
1. Check priority levels
2. Check queue depth (demand)
3. Check wait time (fairness)
4. Allocate to highest scorer

**Decision Process**:
```
Priority(M2) = NORMAL, Queue(M2) = 5, Wait(M2) = 0s
Priority(M3) = NORMAL, Queue(M3) = 2, Wait(M3) = 0s

Score(M2) = 5 (queue depth)  → Allocate to M2
Score(M3) = 2 (queue depth)  → Queue for M3

After M2 finishes:
Score(M3) = 2 + 15s (aging) → Promote M3
Allocate Tool_A to M3
```

**Key Insight**: Fair resource allocation prevents starvation

### Scenario 6: Cascading Failures

**Duration**: ~10 seconds from first failure to recovery

**Situation**: One failure triggers chain of dependent failures

**Cascade Example**:
```
T=0:   Transport FAILS
T=1:   M2 backs up (queue fills)
T=2:   Buffer overflows
T=3:   M1 cannot place parts (stalls)
T=4:   M1 and M2 both FAILED (cascaded)
T=5:   Coordinator detects root cause
T=6:   ALERT: Multiple failures, root: Transport
T=7:   Recovery plan: Repair Transport
T=8:   Transport REPAIRED
T=9:   M1, M2 RESET and RESTARTED
T=10:  System stabilized
```

**Root Cause Analysis**:
```java
List<FailedMachines> = [M1, M2, Transport];
FailedMachines root = findRootCause(FailedMachines);
// Returns: Transport (primary cause)

Cascade chain:
  Transport FAILS
    ↓ causes
  M2 backs up
    ↓ causes
  M1 stalls
    ↓ results in
  REPORTED: M1 FAILED, M2 FAILED
```

**Key Insight**: Root cause analysis identifies real issue, not symptoms

---

## Advanced Reconfiguration Scenarios

### Scenario A: Production Peak (High Demand)

**Context**: Three urgent orders arrive simultaneously
- Order A: 100 units in 15 minutes
- Order B: 150 units in 15 minutes
- Order C: 75 units in 20 minutes
- **Total: 325 units**

**Challenge**: Current capacity is ~25 units per 15 minutes

**Strategy: ACCELERATION_MODE**

**Actions**:
1. **M2 - Reduce cycle time**: 5s → 3s
   - Skip non-critical checks
   - Risk: Slightly lower quality (acceptable)
   - Benefit: 40% throughput increase

2. **M3 - Enable parallel processing**: 1 lane → 2 lanes
   - Run two assemblies simultaneously
   - Requirement: Double component supply
   - Benefit: 100% throughput increase

3. **M4 - Sample testing**: 100% → 10% inspection
   - Reduce from 2s → 0.5s per unit
   - Benefit: 75% time reduction

4. **Component supply**: Stage parts, eliminate waiting

**Result**:
```
Original throughput: ~25 units / 15min
Peak mode throughput: ~350 units / 15min (14x increase)

Timeline:
  T=0min:   Start peak mode
  T=5min:   125 units completed (ahead of schedule)
  T=10min:  250 units completed (ahead of schedule)
  T=15min:  325 units completed ✓ ALL ORDERS MET
  T=25min:  Return to normal mode
```

**Key Decisions**:
- Trade quality for speed (recoverable)
- Increase risk slightly (managed)
- Use maximum capacity (acceptable for urgent orders)

### Scenario B: Product Change (Alpha → Beta)

**Context**: Market shift requires product switch
- Current: Alpha (simple, 12s per unit)
- Target: Beta (complex, 14s per unit)
- Requirement: No downtime for changeover

**Changeover Procedure**:

**Phase 1 - Preparation (5 minutes)**:
1. Stop accepting new Alpha orders
2. Drain M1→M2 pipeline
3. Complete all Alpha units in progress
4. Verify Beta component availability

**Phase 2 - Reconfiguration (10 minutes)**:
```
M2 (3 min):
  Current: Alpha machining profile
  Update:  Beta machining profile
  Changes: Tool selection, speed curves

M3 (5 min):
  Current: Frame + Motor + Housing
  Update:  + Sensor integration + Wiring
  Changes: New gripper positions, calibration

M4 (2 min):
  Current: Basic checks
  Update:  + Sensor functionality test
  Changes: New test procedures
```

**Phase 3 - Verification (10 minutes)**:
1. Trial runs with dummy/real parts
2. Verify all systems report "READY"
3. Sample first 5 units for quality

**Phase 4 - Ramp-Up (15 minutes)**:
```
Min 0-5:   Small batch (5 units) - Monitor for issues
Min 5-15:  Medium batch (20 units) - Verify quality, timing
Min 15+:   Full production (normal pace)
```

**Result**:
```
Total downtime: ~40 minutes (shared setup + verification + ramp)
Production loss: Acceptable for planned changeover
First full Beta unit: ~20 minutes from start
Quality: 99%+ pass rate achieved
```

**Contingency - Rollback**:
If critical issues discovered:
- Restore M2 Alpha profile (3 min)
- Restore M3 Alpha assembly (5 min)
- Restore M4 Alpha QC (2 min)
- Resume Alpha production
- **Rollback time: ~5 minutes**

**Key Decisions**:
- Schedule changeover during low-demand period
- Prepare components in advance
- Perform extensive verification
- Maintain rollback capability

### Scenario C: Graceful Degradation

**Context**: M2 experiencing gradual deterioration, must maintain production

**Failure Progression**:
```
T=0h:   OPERATIONAL (cycle: 5s)
T=2h:   WARNING: Vibration detected (cycle: 5.5s, +10%)
T=4h:   ALERT: Efficiency drops (cycle: 6s, +20%)
T=6h:   CRITICAL: Accuracy degrading (quality checks needed)
T=8h:   URGENT: Only 75% output (maintenance REQUIRED)
T=10h:  FAILURE: Complete breakdown likely
```

**Stage 1 - Early Warning (T=2h)**:
```
Detection: Monitor alerts supervisor
Response:  Schedule preventive maintenance
Action:    Plan maintenance window tomorrow
Status:    No production changes
```

**Stage 2 - Degradation Detected (T=4h)**:
```
Detection: Cycle time degraded to 6s
Response:  Implement load redistribution
Actions:
  - ACCELERATE M1 (+20% speed)
  - REDUCE_LOAD M3 (pre-buffer)
  - Add buffer capacity (stage more parts)
Result:    Maintains throughput despite degradation
```

**Stage 3 - Critical Threshold (T=6h)**:
```
Detection: Accuracy at 85%, quality issues appear
Response:  Activate EMERGENCY_MAINTENANCE_MODE
Actions:
  - M4 increases sampling to 50% (catch defects)
  - Reduce M2 speed further (reduce stress)
  - Prepare M2_backup for switchover
  - Notify maintenance: IMMEDIATE service
Result:    System stable but degraded
```

**Stage 4 - Imminent Failure (T=8h)**:
```
Detection: M2 at 75% capacity, failure in 2h
Response:  Prepare for bypass
Actions:
  - STOP new orders to M2
  - Activate emergency buffer
  - Reroute planning: M1→M3 directly if needed
  - Deploy maintenance team
Result:    Ready for seamless transition
```

**Stage 5 - Failure and Recovery (T=9h)**:
```
M2: FAILED
Response: Activate prepared bypass
Status:   Production continues uninterrupted
          (buffered parts available for M3)
```

**Comparison**:
```
Graceful Degradation:
  - 8 hours advance warning
  - Planned maintenance window
  - Smooth production adjustment
  - Minimal quality impact
  - Cost: Reduced efficiency + prevention (~$700)

vs Abrupt Failure (no early detection):
  - Sudden stoppage
  - Emergency rerouting
  - Queue overflow
  - Downstream stalls
  - Cost: Downtime loss (~$5,000)
```

**Key Insight**: Early detection enables proactive response

### Scenario D: Component Supply Chain Failure

**Context**: Critical component supplier disrupted for 4 hours

**Supply Chain**:
```
Normal:
  Frame:    1/min from Supplier_A
  Motor:    1/min from Supplier_B (PRIMARY) [FAILS]
  Housing:  1/min from Supplier_C
  Fastener: 4/min from Supplier_D

Buffer:  5-unit safety stock for motors
```

**Problem Timeline**:
```
T=0:     Supplier_B: "No motors for 4 hours"
T=1:     M3 inventory: 4 motors remaining
T=2:     M3 consumption: 0.33 units/sec
T=12:    Stockout: No motors left (buffer depleted)
```

**Response Strategy**:

**Option A - Emergency Supplier** (CHOSEN):
```
Supplier_E (premium):
  - Cost: 50% more expensive
  - Delivery: 1 hour
  - Quantity: Up to 20 units

Actions:
  1. ACTIVATE emergency order (20 motors)
  2. REDUCE M3 production rate: 50%
  3. EXTEND buffer: Slow consumption

Timeline:
  T=0-60:  M3 at 50%, stretches motors
  T=60:    Emergency motors arrive
  T=65:    M3 returns to full speed
  T=120:   Supplier_B back online

Cost: $500 (emergency markup) + $200 (lost production)
Total: ~$700
```

**Option B - Full Shutdown** (NOT CHOSEN):
```
Actions:
  1. STOP M3
  2. QUEUE M1, M2 output
  3. DRAIN M4
  4. WAIT 4 hours

Cost: 4 hours downtime = ~$5,000 lost production
```

**Savings**: $4,300 (Option A vs Option B)

**Key Decision**: Activate emergency supplier
- Higher short-term cost
- Prevents catastrophic downtime
- Maintains customer relationships
- Absorbs cost through volume discount negotiations

---

## Running the Demonstrations

### Compilation
```bash
javac -d bin src/*.java
```

### Execution Modes

**1. Full Demo (default)**:
```bash
java -cp bin App
```
Runs:
- All 3 RLRA architecture demonstrations
- All 6 inter-agent interaction scenarios
- All 4 advanced reconfiguration scenarios
**Duration**: ~10-15 minutes

**2. Architecture Only**:
```bash
java -cp bin App arch
```
Runs only the three RLRA architecture demonstrations
**Duration**: ~5 minutes

**3. Interaction Scenarios**:
```bash
java -cp bin App interactions
```
Runs all 6 inter-agent interaction scenarios
**Duration**: ~5 minutes

**4. Reconfiguration Scenarios**:
```bash
java -cp bin App scenarios
```
Runs all 4 advanced reconfiguration scenarios
**Duration**: ~5 minutes

**5. Help**:
```bash
java -cp bin App help
```
Displays usage information

### Expected Output

Each scenario prints:
1. **Header** with scenario title and number
2. **Context** explaining the situation
3. **Numbered steps** with descriptions and messages
4. **Results** and key insights
5. **Completion marker** (✓)

### Output Example
```
======================================================================
SCENARIO 2: Multi-Site Coordination
======================================================================

1. Normal state - Both sites operational:
   Site A: M1_Distribution (OPERATIONAL), M2_Machining (OPERATIONAL)
   Site B: M3_Assembly (OPERATIONAL), M4_QualityControl (OPERATIONAL)

2. Failure at Site A:
   M2_Machining stops working
   Monitor_A detects and sends alert to Coordinator_A
...
✓ Scenario 2 completed
```

---

## Summary

### Custom Agents
- **ConveyorAgent**: Transport between sites, subject to failures
- **AssemblyAgent**: Complex assembly with component management and product changes

### Interaction Scenarios (6 total)
1. Simple failure detection
2. Multi-site coordination
3. Conflict resolution
4. Sequential dependencies
5. Resource sharing
6. Cascading failures

### Reconfiguration Scenarios (4 total)
1. Production peak (high demand)
2. Product change (Alpha → Beta)
3. Graceful degradation (proactive maintenance)
4. Supply chain failure (component shortage)

### Key Concepts Demonstrated
- **Hierarchical decision-making**: Local → Global
- **Conflict resolution**: Priority-based, capacity-aware
- **Proactive vs reactive**: Early detection vs emergency response
- **Trade-offs**: Quality vs speed, cost vs risk
- **System resilience**: Graceful handling of failures

### Real-World Applicability
These scenarios represent actual factory automation challenges and show how a multi-agent system can handle them intelligently.
