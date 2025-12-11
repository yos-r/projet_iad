# Communication Protocol and Conflict Management

## Table of Contents
1. [ACL Protocol Specification](#acl-protocol-specification)
2. [Message Types and Formats](#message-types-and-formats)
3. [Inter-Agent Interaction Scenarios](#inter-agent-interaction-scenarios)
4. [Conflict Resolution Strategy](#conflict-resolution-strategy)
5. [Message Flow Examples](#message-flow-examples)

---

## ACL Protocol Specification

### Overview
The factory system uses an Agent Communication Language (ACL) based protocol inspired by FIPA ACL standard. Messages are exchanged asynchronously through a central MessageBroker.

### Message Structure

```java
public class Message {
    private String senderId;           // Unique agent identifier
    private String receiverId;         // Target agent identifier
    private MessageType type;          // Category of message
    private Object payload;            // Message content
    private long timestamp;            // Creation time (ms)
}
```

### Key Characteristics
- **Asynchronous**: Sender doesn't wait for response
- **Reliable**: Messages queued in MessageBroker mailbox
- **Typed**: Each message has explicit type for routing
- **Timestamped**: Arrival order guaranteed
- **Flexible Payload**: Can carry any serializable object

---

## Message Types and Formats

### 1. STATE_UPDATE
**Purpose**: Agents report status changes
**Direction**: Machine/Conveyor → Monitor → RLRA
**Payload**: State object or string description

```
Example Message:
{
  senderId: "M2_Machining",
  receiverId: "Monitor_SiteA",
  type: STATE_UPDATE,
  payload: MachineState {
    status: OPERATIONAL,
    cycleTime: 5,
    utilization: 0.85,
    itemsProcessed: 234
  }
}
```

**Typical Transitions**:
- Machine operational with metrics
- Machine degraded (performance drop)
- Machine failed (error condition)
- Machine in maintenance

**Handler**: MonitorAgent.handleStateUpdate()

---

### 2. RECONFIGURATION_REQUEST
**Purpose**: Monitor notifies controller of anomaly requiring decision
**Direction**: Monitor → RLRA
**Payload**: Map with details of issue

```java
{
  senderId: "Monitor_SiteA",
  receiverId: "RLRA_Main",
  type: RECONFIGURATION_REQUEST,
  payload: {
    affectedMachine: "M2_Machining",
    issue: "Spindle bearing failure",
    siteId: "SITE_A",
    affectedMachines: ["M1", "M2"],
    severity: "CRITICAL",
    timestamp: 1234567890000
  }
}
```

**Severity Levels**:
- **WARNING**: Performance degradation, monitor closely
- **ALERT**: Service required soon, plan maintenance
- **CRITICAL**: Immediate action needed, potential shutdown
- **EMERGENCY**: Safety risk or cascading failure

**Handler**: RLRACentralized.handleReconfigurationRequest()

---

### 3. RECONFIGURATION_PLAN
**Purpose**: RLRA notifies monitors of chosen strategy
**Direction**: RLRA → Monitor
**Payload**: Strategy name and parameters

```
{
  senderId: "RLRA_Main",
  receiverId: "Monitor_SiteA",
  type: RECONFIGURATION_PLAN,
  payload: "STRATEGY_BYPASS_M2:activate_emergency_buffer:increase_M1_rate:0.8"
}
```

**Common Strategies**:
- `STRATEGY_BYPASS` - Disable machine, route around
- `STRATEGY_REASSIGNMENT` - Transfer work to backup
- `STRATEGY_ACCELERATION` - Increase speed of other machines
- `STRATEGY_LOAD_BALANCING` - Redistribute work
- `STRATEGY_PRODUCTION_PEAK_MODE` - Handle surge demand
- `STRATEGY_PRODUCT_CHANGE` - Switch products
- `STRATEGY_GRACEFUL_DEGRADATION` - Progressive failure handling

**Handler**: MonitorAgent.handleReconfigurationPlan()

---

### 4. EXECUTE_ACTION
**Purpose**: Controller sends specific command to machine
**Direction**: RLRA/Coordinator → Machine/Conveyor
**Payload**: Action command string

```
Example Actions:
- "PROCESS" - Normal processing
- "BYPASS" - Skip this machine
- "ACCELERATE" - Speed up
- "REDUCE_SPEED:0.8" - Slow to 80%
- "STOP" - Pause operation
- "START" - Resume operation
- "RESET" - Return to initial state
- "EMERGENCY_STOP" - Immediate halt
- "PARALLEL_MODE:2_LANES" - Enable parallel processing
- "SAMPLE_TESTING:10_PERCENT" - Reduce QC intensity
- "SUPPLY_COMPONENT:MOTOR:10" - Assembly: supply components
- "CHANGE_PRODUCT:Beta" - Assembly: switch product
- "REDUCE_RATE:0.5" - Production: operate at 50%
```

**Handler**: BaseAgent.handleMessage() → specific executeAction()

---

### 5. ACTION_RESULT
**Purpose**: Machine reports command execution result
**Direction**: Machine/Conveyor → RLRA/Monitor
**Payload**: Result status and metrics

```
{
  senderId: "M2_Machining",
  receiverId: "RLRA_Main",
  type: ACTION_RESULT,
  payload: "Action ACCELERATE completed: cycle_time=3s, status=OPERATIONAL"
}
```

**Success Indicators**:
- Action accepted and executing
- Action completed successfully
- New state confirmed

**Error Indicators**:
- Cannot accept action (wrong state)
- Action failed during execution
- Invalid parameters

---

### 6. CONFLICT_REPORT
**Purpose**: Local coordinator escalates conflict to supervisor
**Direction**: Coordinator → Supervisor
**Payload**: Conflict details

```java
{
  senderId: "Coordinator_A",
  receiverId: "Supervisor",
  type: CONFLICT_REPORT,
  payload: {
    siteId: "SITE_A",
    issue: "Dual machine failure with no local recovery",
    affectedMachines: ["M1", "M2"],
    localDecision: "BYPASS_BOTH",
    constraint: "No backup available at site",
    impactEstimate: "50% capacity loss",
    urgency: "CRITICAL"
  }
}
```

**Conflict Types**:
- Resource contention (multiple requests for same tool)
- Capacity exceeded (queue overflow)
- Cascading failures (multiple failures)
- Supply chain disruption (component shortage)
- Incompatible decisions (site-level conflicts)

**Handler**: RLRADistributed.SiteCoordinator.reportConflict()

---

### 7. CONFLICT_RESOLUTION
**Purpose**: Supervisor sends decision to resolve conflict
**Direction**: Supervisor → Coordinator
**Payload**: Resolution command

```
{
  senderId: "Supervisor",
  receiverId: "Coordinator_A",
  type: CONFLICT_RESOLUTION,
  payload: "REDIRECT_TO_SITE_B: bypass_site_a, reroute_transport"
}
```

**Resolution Strategies**:
- `APPROVED` - Local decision approved
- `OVERRIDE` - Supervisor decision replaces local
- `ESCALATE` - Further human intervention needed
- `EMERGENCY_MODE` - Activate failover procedures

**Handler**: RLRADistributed.SiteCoordinator.handleConflictResolution()

---

### 8. HEARTBEAT
**Purpose**: Periodic health check and status confirmation
**Direction**: All agents → MessageBroker
**Payload**: Agent status summary

```
{
  senderId: "M1_Distribution",
  receiverId: "Monitor_SiteA",
  type: HEARTBEAT,
  payload: "ALIVE: status=OPERATIONAL, load=0.6"
}
```

**Interval**: Every 10 seconds
**Timeout**: If no heartbeat for 30 seconds, agent considered offline

---

## Inter-Agent Interaction Scenarios

### Scenario 1: Simple Failure Detection

```
Timeline:
  T=0ms:   Machine stops (error signal)
  T=10ms:  Monitor polls state → detects FAILED status
  T=20ms:  Monitor sends RECONFIGURATION_REQUEST
  T=50ms:  RLRA receives and analyzes
  T=100ms: RLRA sends RECONFIGURATION_PLAN
  T=120ms: Monitor receives and notifies system
  T=150ms: Machines begin executing strategy
  T=200ms: System stabilized in new configuration
```

**Key Points**:
- Detection latency: 10-20ms (poll interval)
- Decision latency: 30-50ms (analysis)
- Execution latency: 50-100ms (propagation)
- Total: ~200ms to new steady state

**Message Sequence**:
```
Machine --STATE_UPDATE--> Monitor
                          |
                          v (analyzes)
                    RECONFIGURATION_REQUEST
                          |
                          v (decides)
                       RLRA
                          |
                          v (strategy)
                    RECONFIGURATION_PLAN
                          |
                          v
                    EXECUTE_ACTION
                          |
                          v (executes)
                       Machine (new behavior)
                          |
                          v
                     ACTION_RESULT
```

### Scenario 2: Multi-Site Coordination

```
Situation: M2 fails at Site A
  Site A: M1 OPERATIONAL, M2 FAILED
  Site B: M3 OPERATIONAL, M4 OPERATIONAL

Decision Path:
  1. Monitor_A → Coordinator_A: RECONFIGURATION_REQUEST
  2. Coordinator_A (local analysis):
     - Can M1 output bypass M2?  YES
     - Is buffer space adequate?  YES
     - Local recovery possible?   YES
  3. Coordinator_A → Site coordinators: LOCAL_DECISION_APPLIED
     No escalation needed
```

**No Escalation Path** (simpler):
- Takes ~100-150ms total
- Minimal supervisor overhead
- Faster recovery

### Scenario 3: Conflict Resolution

```
Situation: Both M1 and M2 fail simultaneously

Escalation Path:
  1. Monitor_A detects M1 FAILED
  2. Monitor_A → Coordinator_A: RECONFIGURATION_REQUEST
  3. Monitor_A detects M2 FAILED
  4. Monitor_A → Coordinator_A: RECONFIGURATION_REQUEST (second)
  5. Coordinator_A analyzes:
     - M1 failed: source blocked
     - M2 failed: processing blocked
     - Both problems: LOCAL CONFLICT
  6. Coordinator_A → Supervisor: CONFLICT_REPORT
     Content: "Dual failure, no local recovery possible"
  7. Supervisor analyzes global state
  8. Supervisor → Coordinator_A: CONFLICT_RESOLUTION
     Content: "BYPASS_SITE_A, reroute to Site B"
  9. Coordinator_A executes global decision
```

**Escalation adds latency**: ~200-300ms vs 150ms local
**Benefit**: Global optimization, prevents site-level thrashing

---

## Conflict Resolution Strategy

### Types of Conflicts

#### 1. Resource Contention
**Problem**: Multiple agents need same resource

**Example**:
```
M2: Requests Tool_A (in use by M1)
M3: Requests Tool_A (also waiting)

Resolution:
  Priority rule: Higher queue depth first
  M2 queue: 10 items
  M3 queue: 3 items
  Decision: Grant to M2, queue M3
```

**Implementation**:
```java
public String resolveResourceConflict(String resource,
                                     List<Agent> requesters) {
    // Sort by queue depth
    requesters.sort((a, b) ->
        b.getQueueLength() - a.getQueueLength());

    // Grant to first, queue others
    return requesters.get(0).getId();
}
```

#### 2. Cascading Failures
**Problem**: One failure causes chain of failures

**Example**:
```
Transport FAILS
  ↓ Parts can't move
  ↓ M2 backs up
  ↓ Queue overflows
  ↓ M1 stalls

Resolution:
  1. Identify root cause: Transport
  2. Fix root: Repair transport
  3. Drain queue: Clear backlog
  4. Restart in sequence: M1→M2→M3
```

**Implementation**:
```java
public void resolveCascadingFailure(String rootCause) {
    // 1. Stop all affected machines
    affectedMachines.forEach(m -> m.stop());

    // 2. Fix root cause
    fixMachine(rootCause);

    // 3. Clear buffers
    clearBuffers();

    // 4. Restart in order
    restartSequence();
}
```

#### 3. Capacity Exceeded
**Problem**: Queue/buffer full, cannot accept more

**Example**:
```
M1→M2 Buffer (capacity: 20 parts)
Current: 18 parts
M1 produces 1 more → Buffer FULL
M1 next cycle: Cannot place part

Resolution:
  - Signal M1 to SLOW_DOWN
  - Wait for M2 to process
  - Resume normal when space available
```

**Implementation**:
```java
if (buffer.isFull()) {
    upstream.send(EXECUTE_ACTION, "REDUCE_RATE:0.5");
    return false; // Cannot accept part
}
```

#### 4. Incompatible Decisions
**Problem**: Local decision violates global constraint

**Example**:
```
Coordinator_A wants: BYPASS_M2
Supervisor says: NO (M2 output needed for Site B)

Resolution:
  Supervisor overrides: PROCESS_NORMALLY
  Coordinator implements supervisor decision
```

**Implementation**:
```java
if (localDecision.conflicts(globalConstraints)) {
    return supervisor.resolve(conflict);
}
```

### Conflict Resolution Algorithms

#### Priority-Based Resolution
```
1. Categorize by severity
2. Higher severity wins
3. Implement higher priority decision

Priority order:
  1. Safety (never compromise)
  2. Quality (core requirement)
  3. Throughput (nice to have)
  4. Cost (optimize if possible)
```

#### Capacity-Based Resolution
```
1. Check available resources
2. Allocate to highest-demand agent
3. Queue others fairly

Resource allocation:
  if (totalDemand > capacity) {
      allocate = (capacity / totalDemand);
      foreach (agent in requesters) {
          grant(agent, agent.demand * allocate);
      }
  }
```

#### Time-Based Resolution
```
1. First come, first served
2. Respect request timestamp
3. Implement aging (avoid starvation)

Fairness:
  if (agentWaitTime > threshold) {
      promote(agent); // Give priority
  }
```

---

## Message Flow Examples

### Example 1: Machine Failure → Reassignment

```
Timeline (ms)  Agent              Message                Status
────────────────────────────────────────────────────────────────────
0              M2                 Status: OPERATIONAL
50             M2                 Status: FAILED        Sensor error
55             Monitor_A          RECONFIGURATION_REQUEST to RLRA
100            RLRA_Main          Analyzes situation    M1 available
105            RLRA_Main          RECONFIGURATION_PLAN  Reassign to M1
110            M1                 EXECUTE_ACTION:       Start processing
                                  PROCESS:extra_parts
150            M1                 ACTION_RESULT:        Confirmed
155            Monitor_A          STATUS_UPDATE         M1 at 120% load
200            RLRA_Main          RECONFIGURATION_PLAN  Balance load
205            M3                 EXECUTE_ACTION:       Reduce intake
250            System             Stabilized            M1↔M2 flow re-routed
```

### Example 2: Production Peak Response

```
Timeline    Phase           RLRA Decision            Machines         Status
──────────────────────────────────────────────────────────────────────
T=0         Alert received  Analyze demand          All normal
T=5         Decision made   ACCELERATION_MODE       Reconfiguring
            - Peak mode
            - Enable parallel
            - Reduce QC
T=10        Execution       Send EXECUTE_ACTION     M2: -40% cycle
                            - M2: speed up          M3: parallel
                            - M3: parallel          M4: sampling
                            - M4: sample
T=20        Ramp-up         Monitor metrics         Output ↑3.5x
T=300       Completion      Verify orders done      All 325 units ✓
T=320       Recovery        Return normal mode      Restore standard
T=330       Stabilized      Back to normal          All systems ✓
```

### Example 3: Conflict Resolution

```
Conflict Scenario: M1 AND M2 FAIL simultaneously

Message Flow:
  │
  ├─ 1. M1 FAILED
  │  └─ Monitor_A → Coordinator_A: RECONFIGURATION_REQUEST
  │
  ├─ 2. M2 FAILED (almost same time)
  │  └─ Monitor_A → Coordinator_A: RECONFIGURATION_REQUEST
  │
  ├─ 3. Coordinator_A (analysis)
  │  ├─ Check local resources: NONE available
  │  └─ Detect CONFLICT
  │
  ├─ 4. Coordinator_A → Supervisor: CONFLICT_REPORT
  │  └─ "Both M1 and M2 failed, no local recovery"
  │
  ├─ 5. Supervisor (global analysis)
  │  ├─ Check Site B: M3, M4 operational
  │  ├─ Decision: BYPASS_SITE_A, REROUTE_TO_SITE_B
  │  └─ Send resolution
  │
  ├─ 6. Supervisor → Coordinator_A: CONFLICT_RESOLUTION
  │  └─ "Execute: bypass_site_a_route_transport_direct"
  │
  └─ 7. Coordinator_A executes global decision
     └─ Production continues with altered flow
```

---

## Implementation Guidelines

### Message Sender Responsibilities
1. Include all necessary context in payload
2. Use appropriate message type
3. Target correct receiver
4. Include timestamp for ordering
5. Handle non-response gracefully

### Message Receiver Responsibilities
1. Verify message format and type
2. Validate payload content
3. Update agent state accordingly
4. Take appropriate action
5. Send confirmation/result if needed

### Broker Responsibilities
1. Queue messages reliably
2. Maintain message ordering
3. Log all messages for audit
4. Handle delivery timeouts
5. Report undeliverable messages

### Agent Lifecycle
```
1. CREATED → Register with broker
2. RUNNING → Process messages in step()
3. STOPPING → Drain queues, send final messages
4. STOPPED → Unregister from broker
```

### Error Handling
```
try {
    message = broker.getMessage(agentId);
    if (message != null) {
        handleMessage(message);
    }
} catch (InvalidMessageException e) {
    // Log and discard
} catch (ActionFailureException e) {
    // Send ACTION_RESULT with error
} catch (Exception e) {
    // Log critical error
    notifyMonitor(ERROR, e.getMessage());
}
```

---

## Protocol Performance

### Latency Metrics
- Message send → receive: ~1ms
- Decision (RLRA): ~30-100ms
- Execution start: ~10-50ms
- **Total response time: ~100-150ms**

### Throughput
- Message broker: ~10,000 msgs/sec
- System bottleneck: Decision making
- Scaling: Add decision modules for parallel processing

### Reliability
- Message loss: None (in-memory queue)
- Duplicate handling: Timestamp-based
- Out-of-order: Handled by timestamps
- Timeout: 5 seconds per message

---

## Summary

The ACL-based protocol provides:
- **Type safety**: Explicit message types
- **Scalability**: Distributed message routing
- **Traceability**: Full message logging
- **Flexibility**: Any payload type supported
- **Reliability**: No loss in happy path

Conflict resolution uses:
- **Hierarchical authority**: Local→Global
- **Priority-based decisions**: Safety first
- **Fair arbitration**: FIFO with aging
- **Escalation paths**: Clear when needed

This design enables complex factory automation scenarios while maintaining system stability and responsiveness.
