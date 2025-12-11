# RLRA Architecture Documentation

This document describes the three implementations of the RLRA (Reconfiguration Logic Reasoning Agent) controller for the FESTO CP Factory dynamic reconfiguration system.

## Overview

The RLRA controller is responsible for making decisions about how to reconfigure the factory when anomalies (machine failures, production peaks, etc.) occur. Three different architectural approaches are implemented to compare their trade-offs.

---

## Architecture 1: Centralized RLRA

**Class**: `RLRACentralized.java`

### Design

A single RLRA agent makes all reconfiguration decisions for the entire factory system.

```
┌─────────────────────────────────────┐
│      RLRA (Centralized)             │
│  ┌────────────────────────────────┐ │
│  │  Decision Engine               │ │
│  │  - Receive requests from       │ │
│  │    all monitors                │ │
│  │  - Analyze situation           │ │
│  │  - Make decisions              │ │
│  │  - Send commands to machines   │ │
│  └────────────────────────────────┘ │
└─────────────────────────────────────┘
        ↓        ↑        ↓
   Monitor      Monitor   Machines
    Sites       Sites    on Sites
```

### Decision Logic Flow

1. **Receive Request**: Monitors detect anomalies and send reconfiguration requests to RLRA
2. **Analyze**: RLRA examines the affected machine and system state
3. **Decide**: Selects a strategy based on the issue type:
   - **Machine Failure** → STRATEGY_REASSIGNMENT (if backup available) or STRATEGY_BYPASS
   - **Performance Issue** → STRATEGY_ACCELERATION
   - **Default** → STRATEGY_BYPASS

4. **Execute**: Sends action commands to the affected machines
5. **Notify**: Informs monitors of the reconfiguration plan

### Strategy Details

- **STRATEGY_BYPASS**: Disable the failed machine and route work around it
- **STRATEGY_REASSIGNMENT**: Transfer work to a backup/backup machine
- **STRATEGY_ACCELERATION**: Reduce cycle times on other machines to compensate

### Advantages

✅ **Simple**: Single decision point, easy to understand and implement
✅ **Deterministic**: Consistent decision-making process
✅ **Centralized Control**: Clear authority and oversight
✅ **Easy to Debug**: All decisions logged in one place

### Disadvantages

❌ **Single Point of Failure**: If RLRA fails, entire system can't reconfigure
❌ **Bottleneck**: All decisions go through one agent, potential performance issue
❌ **Scalability**: Harder to add new machines/sites efficiently
❌ **Latency**: Distance between monitors and central decision-maker

### When to Use

- Small factory systems with few machines
- Environments where consistency is critical
- Development and testing phases
- Systems with reliable infrastructure

---

## Architecture 2: Modular (Composite) RLRA

**Class**: `RLRAModular.java`

### Design

The RLRA controller is composed of three specialized modules that work together.

```
┌──────────────────────────────────────────┐
│         RLRA (Modular)                   │
│  ┌────────────────────────────────────┐  │
│  │  Monitor Module                    │  │
│  │  - Collects state from all         │  │
│  │    monitors                        │  │
│  │  - Maintains system state          │  │
│  └────────────────────────────────────┘  │
│  ┌────────────────────────────────────┐  │
│  │  Learner Module                    │  │
│  │  - Analyzes situations             │  │
│  │  - Makes reconfiguration decisions │  │
│  │  - Optimization logic              │  │
│  └────────────────────────────────────┘  │
│  ┌────────────────────────────────────┐  │
│  │  Executor Module                   │  │
│  │  - Executes plans                  │  │
│  │  - Sends commands to machines      │  │
│  │  - Tracks execution status         │  │
│  └────────────────────────────────────┘  │
└──────────────────────────────────────────┘
            ↓        ↑        ↓
        Monitors   Machines  Machines
        (State)    (Commands) (Status)
```

### Module Responsibilities

**Monitor Module**:
- Receives reconfiguration requests
- Maintains knowledge of current factory state
- Tracks machine statuses and capabilities
- No decision-making responsibility

**Learner Module**:
- Analyzes the current situation
- Applies decision rules
- Can be enhanced with machine learning algorithms
- Produces reconfiguration strategies

**Executor Module**:
- Receives decisions from Learner
- Executes the plan by sending commands
- Monitors execution progress
- Reports results back

### Decision Flow

```
Request → Monitor Module → Learner Module → Executor Module → Actions
  ↓
State Update
```

### Advantages

✅ **Separation of Concerns**: Each module has single responsibility
✅ **Modularity**: Modules can be developed/tested independently
✅ **Extensibility**: Easy to enhance Learner with ML algorithms
✅ **Reusability**: Modules can be reused in different contexts
✅ **Testability**: Each module can be unit tested separately

### Disadvantages

❌ **More Complex**: Inter-module communication overhead
❌ **Latency**: Multiple stages add processing time
❌ **Debugging**: Harder to trace issues across modules
❌ **State Consistency**: Must ensure state synchronization between modules

### When to Use

- Medium-sized systems that need flexibility
- When decision logic needs frequent updates
- Systems where monitoring/execution/decision-making are separate concerns
- Production environments where parts need to be upgraded independently

---

## Architecture 3: Distributed RLRA

**Class**: `RLRADistributed.java`

### Design

Decision-making is distributed across the factory. Each site has a **Coordinator** that makes local decisions, and a global **Supervisor** resolves inter-site conflicts.

```
                    ┌──────────────────┐
                    │  Supervisor      │
                    │ - Global view    │
                    │ - Resolve        │
                    │   inter-site     │
                    │   conflicts      │
                    └────────┬─────────┘
                             ↑
                    ┌────────┴─────────┐
                    ↓                  ↓
        ┌──────────────────┐  ┌──────────────────┐
        │  Coordinator     │  │  Coordinator     │
        │  Site A          │  │  Site B          │
        │ - Monitor local  │  │ - Monitor local  │
        │   machines       │  │   machines       │
        │ - Make local     │  │ - Make local     │
        │   decisions      │  │   decisions      │
        │ - Detect local   │  │ - Detect local   │
        │   conflicts      │  │   conflicts      │
        └────────┬─────────┘  └────────┬─────────┘
                 ↓                     ↓
        ┌──────────────┐      ┌──────────────┐
        │M1, M2 (Site A)      │M3, M4 (Site B)
        └──────────────┘      └──────────────┘
```

### Agent Types

**Site Coordinator**:
- Monitors machines at a specific site
- Makes local reconfiguration decisions
- Detects conflicts between local decisions and reports to Supervisor
- Implements local optimization strategies

**Supervisor**:
- Maintains global factory state across all sites
- Receives conflict reports from Coordinators
- Resolves inter-site conflicts using global optimization
- Can reassign work between sites
- Has final authority on system-level decisions

### Decision Flow - Normal Case (No Conflict)

```
Local Anomaly → Coordinator Decision → Execute Locally
```

### Decision Flow - Conflict Case

```
Local Anomaly → Coordinator Decision
              ↓
         Conflict Detected?
         / (Yes)    \ (No)
        ↓            ↓
    Report to    Execute
    Supervisor   Locally
        ↓
    Supervisor Analyzes
        ↓
    Send Resolution to Coordinator
        ↓
        Execute Supervisor's Plan
```

### Conflict Detection Examples

- Coordinator wants to bypass M2, but M1 and M2 are only operational machines at site
- Multiple machines fail simultaneously
- Resource constraints make local solution impossible

### Advantages

✅ **Scalability**: Easy to add new sites and machines
✅ **Resilience**: Local failures don't prevent other sites from operating
✅ **Latency**: Local decisions made instantly, no central bottleneck
✅ **Flexibility**: Can customize decision logic per site
✅ **Parallel Processing**: Multiple sites make decisions simultaneously

### Disadvantages

❌ **Complexity**: Distributed coordination is harder to implement
❌ **Consistency**: Must ensure decisions don't conflict globally
❌ **Debugging**: Harder to trace issues across distributed system
❌ **Network Dependent**: Requires reliable inter-site communication
❌ **Potential Inefficiency**: Local decisions might not be globally optimal

### When to Use

- Large factory systems with multiple sites
- High-availability requirements
- Geographically distributed manufacturing
- Systems that need to scale horizontally
- Where local autonomy is important

---

## Comparison Table

| Aspect | Centralized | Modular | Distributed |
|--------|-------------|---------|-------------|
| **Complexity** | Low | Medium | High |
| **Decision Speed** | Medium | Medium-Slow | Fast (Local) |
| **Global Optimality** | High | High | Medium |
| **Scalability** | Low | Medium | High |
| **Resilience** | Low | Low-Medium | High |
| **Implementation Effort** | Low | Medium | High |
| **Testing Complexity** | Low | Medium | High |
| **Single Point of Failure** | Yes | Yes | No |
| **Message Overhead** | Low | Medium | High |

---

## Message Flow Example: Machine Failure Scenario

### Centralized Architecture

```
1. Machine M2 fails (generates error signal)
2. Monitor_SiteA detects failure
3. Monitor_SiteA → RLRA_Main: RECONFIGURATION_REQUEST
4. RLRA_Main: Analyzes situation, decides STRATEGY_REASSIGNMENT
5. RLRA_Main → M1, M3: Send execution commands
6. RLRA_Main → Monitor_SiteA, Monitor_SiteB: RECONFIGURATION_PLAN
7. Machines execute commands
8. Machines → RLRA_Main: ACTION_RESULT
```

### Modular Architecture

```
1. Machine M2 fails
2. Monitor_SiteA detects failure
3. Monitor_SiteA → RLRA_Monitor_Module: RECONFIGURATION_REQUEST
4. RLRA_Monitor_Module → RLRA_Learner_Module: Current state + request
5. RLRA_Learner_Module: Decides STRATEGY_REASSIGNMENT
6. RLRA_Learner_Module → RLRA_Executor_Module: Execute plan
7. RLRA_Executor_Module → M1, M3: Send commands
8. RLRA_Executor_Module ← Machines: ACTION_RESULT
```

### Distributed Architecture

```
1. Machine M2 fails (at Site A)
2. Monitor_SiteA detects failure
3. Monitor_SiteA → Coordinator_A: RECONFIGURATION_REQUEST
4. Coordinator_A: Makes local decision STRATEGY_BYPASS
5. Coordinator_A: Detects conflict (only M1 remains operational)
6. Coordinator_A → Supervisor: CONFLICT_REPORT
7. Supervisor: Analyzes inter-site situation
8. Supervisor: Decides REASSIGN_TO_SITE_B
9. Supervisor → Coordinator_A: CONFLICT_RESOLUTION
10. Coordinator_A → M3 (Site B): Transfer work
```

---

## Implementation Notes

### Class Hierarchy

```
Agent (Interface)
  ↓
BaseAgent (Abstract class)
  ├─ RLRACentralized
  ├─ RLRAModular
  │   ├─ MonitorModule (inner)
  │   ├─ LearnerModule (inner)
  │   └─ ExecutorModule (inner)
  ├─ RLRADistributed
  │   ├─ SiteCoordinator (inner, extends BaseAgent)
  │   └─ Supervisor (inner)
  ├─ MachineAgent
  └─ MonitorAgent
```

### Message Types

- `STATE_UPDATE`: Monitor sends machine state
- `RECONFIGURATION_REQUEST`: Monitor requests reconfiguration from RLRA
- `RECONFIGURATION_PLAN`: RLRA sends plan to monitors
- `EXECUTE_ACTION`: Executor sends action command to machine
- `ACTION_RESULT`: Machine reports execution result
- `CONFLICT_REPORT`: Coordinator reports conflict to supervisor
- `CONFLICT_RESOLUTION`: Supervisor sends conflict resolution

### Key Design Patterns

1. **Message Broker**: Central message queue for inter-agent communication
2. **Factory Pattern**: RLRAFactory creates appropriate architecture
3. **Strategy Pattern**: Different reconfiguration strategies (Bypass, Reassignment, Acceleration)
4. **Observer Pattern**: Monitors observe machine state changes

---

## Running the Demo

```bash
javac -d bin src/*.java
java -cp bin App
```

This will:
1. Create and run a simulation with CENTRALIZED architecture
2. Simulate a machine failure and show reconfiguration
3. Repeat with MODULAR architecture
4. Repeat with DISTRIBUTED architecture
5. Print detailed reports for each

---

## Future Extensions

### Potential Enhancements

1. **Add more strategies**: Include more sophisticated reconfiguration options
2. **Machine Learning**: Use Learner module to train decision models
3. **Multi-objective Optimization**: Optimize for energy, time, cost simultaneously
4. **Predictive Maintenance**: Predict failures before they occur
5. **Dynamic Agent Creation**: Add/remove agents at runtime
6. **Performance Metrics**: Track optimization metrics across architectures
7. **Fault Tolerance**: Implement recovery mechanisms
8. **Scenario Library**: Pre-defined complex scenarios for testing
9. **Visualization**: Add visual representation of factory state
10. **Real-time Communication**: Replace message broker with actual network communication

---

## References

- FESTO CP Factory Specification: See `promp.md`
- Multi-agent System Architecture: See `CLAUDE.md`
- Source Code: See individual Java files
