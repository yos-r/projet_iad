# Usage Examples and Code Patterns

This document provides practical examples of how to use the RLRA architecture implementation.

## Basic Setup

### Running the Default Demo

```bash
javac -d bin src/*.java
java -cp bin App
```

This automatically runs all three architectures with predefined scenarios.

## Creating Custom Simulations

### Example 1: Centralized Architecture Simulation

```java
import java.util.*;

public class CentralizedDemo {
    public static void main(String[] args) {
        // Create simulation with centralized architecture
        FactorySimulation simulation = new FactorySimulation(
            RLRAFactory.ArchitectureType.CENTRALIZED
        );

        // Initialize factory with machines and monitors
        simulation.initializeFactory();

        // Run normal operation
        simulation.run(5);

        // Simulate machine failure
        simulation.simulateMachineFailure("M2_Machining", "Belt slippage");

        // Continue operation after failure
        simulation.run(5);

        // Print detailed report
        simulation.printReport();

        // Clean up
        simulation.stop();
    }
}
```

### Example 2: Testing Multiple Failures

```java
public class MultipleFailuresDemo {
    public static void main(String[] args) {
        FactorySimulation simulation = new FactorySimulation(
            RLRAFactory.ArchitectureType.MODULAR
        );

        simulation.initializeFactory();
        simulation.run(2);

        // First failure
        System.out.println("\n>>> Simulating first failure <<<");
        simulation.simulateMachineFailure("M1_Distribution", "Motor overheating");
        simulation.run(2);

        // Second failure on different site
        System.out.println("\n>>> Simulating second failure <<<");
        simulation.simulateMachineFailure("M4_QualityControl", "Sensor malfunction");
        simulation.run(2);

        simulation.printReport();
        simulation.stop();
    }
}
```

### Example 3: Comparing Architectures Side by Side

```java
public class ArchitectureComparison {
    public static void main(String[] args) {
        String[] machines = {"M1_Distribution", "M2_Machining"};
        String[] errors = {"Motor failure", "Hydraulic leak"};
        int steps = 5;

        Map<String, Long> executionTimes = new HashMap<>();

        for (RLRAFactory.ArchitectureType type :
             RLRAFactory.ArchitectureType.values()) {

            System.out.println("\n=== Testing " + type.name() + " ===");

            FactorySimulation simulation = new FactorySimulation(type);
            simulation.initializeFactory();

            long startTime = System.currentTimeMillis();

            simulation.run(steps);
            for (int i = 0; i < machines.length; i++) {
                simulation.simulateMachineFailure(machines[i], errors[i]);
                simulation.run(steps);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            executionTimes.put(type.name(), executionTime);

            simulation.printReport();
            simulation.stop();
        }

        System.out.println("\n=== Performance Comparison ===");
        executionTimes.forEach((arch, time) ->
            System.out.println(arch + ": " + time + "ms")
        );
    }
}
```

## Working with Individual Agent Types

### Creating Custom Machines

```java
public class CustomMachineExample {
    public static void main(String[] args) {
        // Create specific machines with custom cycle times
        MachineAgent lathe = new MachineAgent("Lathe_01", 8);
        MachineAgent gripper = new MachineAgent("Gripper_01", 3);
        MachineAgent mill = new MachineAgent("Mill_01", 10);

        // Get and inspect machine state
        MachineState lathecState = lathe.getState();
        System.out.println("Machine ID: " + lathecState.getMachineId());
        System.out.println("Cycle Time: " + lathecState.getCycleTime() + "s");
        System.out.println("Status: " + lathecState.getStatus());

        // Simulate operation
        lathecState.setUtilization(0.85);
        lathecState.setCurrentProduct("Part_A123");
        lathecState.setItemsProcessed(42);

        System.out.println("Updated state: " + lathecState);
    }
}
```

### Creating Monitors for Specific Sites

```java
public class MonitorExample {
    public static void main(String[] args) {
        // Create monitor for manufacturing site
        MonitorAgent monitor = new MonitorAgent("Monitor_Plant1", "PLANT_1", "RLRA_Main");

        // Register machines with the monitor
        MachineAgent m1 = new MachineAgent("Drill_01", 5);
        MachineAgent m2 = new MachineAgent("Press_01", 4);
        MachineAgent m3 = new MachineAgent("Welder_01", 6);

        monitor.registerMachine("Drill_01", m1.getState());
        monitor.registerMachine("Press_01", m2.getState());
        monitor.registerMachine("Welder_01", m3.getState());

        // Query monitored machines
        System.out.println("Machines on " + monitor.getId() + ":");
        for (String machineId : monitor.getMonitoredMachines().keySet()) {
            System.out.println("  - " + machineId);
        }

        // Clean up
        monitor.stop();
    }
}
```

## Working with Specific Architectures

### Centralized Architecture Details

```java
public class CentralizedExample {
    public static void main(String[] args) {
        RLRACentralized rlra = new RLRACentralized("Central_Controller");

        // Create machines and monitors
        MachineAgent m1 = new MachineAgent("M1", 2);
        MachineAgent m2 = new MachineAgent("M2", 5);
        MonitorAgent monitor = new MonitorAgent("Monitor_1", "SITE_A", "Central_Controller");

        // Register with RLRA
        rlra.registerMachine(m1);
        rlra.registerMachine(m2);
        rlra.registerMonitor(monitor);

        monitor.registerMachine("M1", m1.getState());
        monitor.registerMachine("M2", m2.getState());

        // Simulate failure
        m2.simulateFailure("Servo error");

        // RLRA checks history of decisions
        System.out.println("Reconfiguration history:");
        for (String decision : rlra.getReconfigurationHistory()) {
            System.out.println("  - " + decision);
        }

        rlra.stop();
        monitor.stop();
    }
}
```

### Modular Architecture Details

```java
public class ModularExample {
    public static void main(String[] args) {
        RLRAModular rlra = new RLRAModular("Modular_Controller");

        // The modular RLRA automatically creates its three modules:
        // - Monitor module (collects state)
        // - Learner module (makes decisions)
        // - Executor module (executes plans)

        MachineAgent m1 = new MachineAgent("M1", 2);
        MonitorAgent monitor = new MonitorAgent("Monitor_1", "SITE_A", "Modular_Controller");

        rlra.registerMachine(m1);
        rlra.registerMonitor(monitor);

        // The modular architecture separates concerns:
        System.out.println("RLRA has three specialized modules:");
        System.out.println("  1. Monitor Module - Receives and maintains state");
        System.out.println("  2. Learner Module - Makes reconfiguration decisions");
        System.out.println("  3. Executor Module - Executes the planned actions");

        // Check execution history
        System.out.println("\nExecution history:");
        for (String execution : rlra.getExecutionHistory()) {
            System.out.println("  - " + execution);
        }

        rlra.stop();
    }
}
```

### Distributed Architecture Details

```java
public class DistributedExample {
    public static void main(String[] args) {
        RLRADistributed rlra = new RLRADistributed("Distributed_Controller");

        // Create site coordinators
        RLRADistributed.SiteCoordinator coordA = rlra.createSiteCoordinator("SITE_A");
        RLRADistributed.SiteCoordinator coordB = rlra.createSiteCoordinator("SITE_B");

        // Create machines at each site
        MachineAgent m1 = new MachineAgent("M1_SiteA", 2);
        MachineAgent m2 = new MachineAgent("M2_SiteA", 5);
        MachineAgent m3 = new MachineAgent("M3_SiteB", 3);
        MachineAgent m4 = new MachineAgent("M4_SiteB", 2);

        // Register machines with their site coordinators
        coordA.registerMachine(m1);
        coordA.registerMachine(m2);
        coordB.registerMachine(m3);
        coordB.registerMachine(m4);

        System.out.println("Distributed RLRA structure:");
        System.out.println("  ├── Supervisor (Global)");
        System.out.println("  ├── Coordinator A");
        System.out.println("  │   ├── M1_SiteA");
        System.out.println("  │   └── M2_SiteA");
        System.out.println("  └── Coordinator B");
        System.out.println("      ├── M3_SiteB");
        System.out.println("      └── M4_SiteB");

        // Access supervisor for global decisions
        RLRADistributed.Supervisor supervisor = rlra.getSupervisor();
        System.out.println("\nSupervisor conflict resolutions:");
        for (String resolution : supervisor.getResolutions()) {
            System.out.println("  - " + resolution);
        }

        rlra.stop();
    }
}
```

## Working with Messages

### Sending Custom Messages

```java
public class MessageExample {
    public static void main(String[] args) {
        // Create agents
        MachineAgent machine = new MachineAgent("M1", 5);
        MonitorAgent monitor = new MonitorAgent("Monitor_1", "SITE_A", "RLRA");

        // Create and send a custom message
        Message stateMsg = new Message(
            "Monitor_1",
            "M1",
            Message.MessageType.STATE_UPDATE,
            "Machine operating normally"
        );

        System.out.println("Message created:");
        System.out.println("  From: " + stateMsg.getSenderId());
        System.out.println("  To: " + stateMsg.getReceiverId());
        System.out.println("  Type: " + stateMsg.getType());
        System.out.println("  Payload: " + stateMsg.getPayload());
        System.out.println("  Timestamp: " + stateMsg.getTimestamp());

        // Send through message broker
        MessageBroker broker = MessageBroker.getInstance();
        broker.registerAgent("RLRA");
        broker.sendMessage(stateMsg);

        // Check if RLRA received message
        if (broker.hasMessages("RLRA")) {
            Message received = broker.getMessage("RLRA");
            System.out.println("\nMessage received by RLRA");
        }

        machine.stop();
        monitor.stop();
    }
}
```

### Monitoring Message Flow

```java
public class MessageMonitoringExample {
    public static void main(String[] args) {
        MessageBroker broker = MessageBroker.getInstance();

        // Create and register agents
        MachineAgent m1 = new MachineAgent("M1", 5);
        MonitorAgent monitor = new MonitorAgent("Monitor", "SITE", "RLRA");
        broker.registerAgent("RLRA");

        // Send some messages
        for (int i = 0; i < 5; i++) {
            Message msg = new Message(
                "Monitor",
                "RLRA",
                Message.MessageType.STATE_UPDATE,
                "Status update " + i
            );
            broker.sendMessage(msg);
        }

        // Get message log
        System.out.println("Message log (" + broker.getMessageLog().size() + " messages):");
        for (Message msg : broker.getMessageLog()) {
            System.out.println("  [" + msg.getTimestamp() + "] " +
                             msg.getSenderId() + " → " + msg.getReceiverId() +
                             " (" + msg.getType() + ")");
        }

        // Print statistics
        broker.printStatistics();

        m1.stop();
        monitor.stop();
        broker.stop();
    }
}
```

## Scenario Scripting

### Script 1: Production Peak Handling

```java
public class ProductionPeakScenario {
    public static void main(String[] args) {
        FactorySimulation sim = new FactorySimulation(
            RLRAFactory.ArchitectureType.MODULAR
        );
        sim.initializeFactory();

        System.out.println("\n=== PRODUCTION PEAK SCENARIO ===");
        System.out.println("Scenario: 3 urgent orders arrive simultaneously");

        // Normal operation
        System.out.println("\n1. Normal production (2 steps)");
        sim.run(2);

        // Production peak - would normally trigger:
        // - Acceleration strategy
        // - Parallelization
        // - Reorganization of work flow
        System.out.println("\n2. Handling production peak");
        System.out.println("   [In full implementation, RLRA would execute:");
        System.out.println("    - STRATEGY_ACCELERATION on bottleneck machines");
        System.out.println("    - Enable parallel processing on M3");
        System.out.println("    - Prioritize urgent orders]");

        sim.run(3);

        System.out.println("\n3. Returning to normal production (2 steps)");
        sim.run(2);

        sim.printReport();
        sim.stop();
    }
}
```

### Script 2: Product Change Scenario

```java
public class ProductChangeScenario {
    public static void main(String[] args) {
        FactorySimulation sim = new FactorySimulation(
            RLRAFactory.ArchitectureType.DISTRIBUTED
        );
        sim.initializeFactory();

        System.out.println("\n=== PRODUCT CHANGE SCENARIO ===");
        System.out.println("Product change from Alpha (simple) to Beta (complex)");

        System.out.println("\n1. Producing Alpha product (2 steps)");
        sim.run(2);

        System.out.println("\n2. Product change initiated");
        System.out.println("   Actions needed:");
        System.out.println("   - Reprogram M2 (machining parameters)");
        System.out.println("   - Adapt M3 (advanced assembly required)");
        System.out.println("   - Update M4 (additional quality checks)");

        System.out.println("\n3. Reconfiguration and changeover (3 steps)");
        sim.run(3);

        System.out.println("\n4. Producing Beta product (2 steps)");
        sim.run(2);

        sim.printReport();
        sim.stop();
    }
}
```

## Testing and Debugging

### Unit Testing Pattern

```java
public class AgentTestExample {
    public static void main(String[] args) {
        // Test MachineAgent
        MachineAgent machine = new MachineAgent("TestMachine", 5);
        assert machine.getId().equals("TestMachine");
        assert machine.getState().getStatus() == MachineState.Status.OPERATIONAL;
        System.out.println("✓ MachineAgent creation test passed");

        // Test failure simulation
        machine.simulateFailure("Test error");
        assert machine.getState().getStatus() == MachineState.Status.FAILED;
        assert machine.getState().getErrorMessage().equals("Test error");
        System.out.println("✓ MachineAgent failure test passed");

        // Test MonitorAgent
        MonitorAgent monitor = new MonitorAgent("TestMonitor", "SITE_A", "RLRA");
        monitor.registerMachine("TestMachine", machine.getState());
        assert monitor.getMonitoredMachines().containsKey("TestMachine");
        System.out.println("✓ MonitorAgent registration test passed");

        machine.stop();
        monitor.stop();
        System.out.println("\nAll tests passed!");
    }
}
```

### Debug Output Pattern

```java
public class DebugExample {
    public static void main(String[] args) {
        FactorySimulation sim = new FactorySimulation(
            RLRAFactory.ArchitectureType.CENTRALIZED
        );
        sim.initializeFactory();

        System.out.println("=== DEBUG MODE ===");
        System.out.println("Monitoring message flow and agent state");

        MessageBroker broker = MessageBroker.getInstance();

        sim.run(1);
        System.out.println("\nAfter 1 step:");
        System.out.println("Messages in broker: " + broker.getMessageLog().size());

        sim.simulateMachineFailure("M1_Distribution", "Debug test failure");

        sim.run(1);
        System.out.println("\nAfter failure and 1 step:");
        System.out.println("Messages in broker: " + broker.getMessageLog().size());

        broker.printStatistics();

        sim.printReport();
        sim.stop();
    }
}
```

## Advanced Usage

### Custom Decision Logic

```java
public class CustomDecisionExample {
    // Extend RLRACentralized to implement custom decision logic
    static class CustomRLRA extends RLRACentralized {
        public CustomRLRA(String id) {
            super(id);
        }

        // Override decision method for custom strategy selection
        // This would require making decidePlan protected instead of private
        // in the base class
    }

    public static void main(String[] args) {
        // Would instantiate and use CustomRLRA with custom logic
        System.out.println("Custom decision logic can be implemented by:");
        System.out.println("1. Extending RLRACentralized class");
        System.out.println("2. Overriding decision methods");
        System.out.println("3. Adding new strategy selection criteria");
    }
}
```

### Multi-Scenario Testing

```java
public class MultiScenarioTest {
    public static void main(String[] args) {
        String[][] scenarios = {
            {"M1_Distribution", "Motor overload"},
            {"M2_Machining", "Spindle bearing failure"},
            {"M3_Assembly", "Gripper stuck"},
            {"M4_QualityControl", "Camera malfunction"}
        };

        for (String[] scenario : scenarios) {
            System.out.println("\n=== Testing " + scenario[1] + " ===");
            FactorySimulation sim = new FactorySimulation(
                RLRAFactory.ArchitectureType.CENTRALIZED
            );
            sim.initializeFactory();
            sim.run(2);
            sim.simulateMachineFailure(scenario[0], scenario[1]);
            sim.run(2);
            sim.stop();
        }
    }
}
```

## Performance Profiling

```java
public class PerformanceExample {
    public static void main(String[] args) {
        for (RLRAFactory.ArchitectureType type :
             RLRAFactory.ArchitectureType.values()) {

            FactorySimulation sim = new FactorySimulation(type);
            sim.initializeFactory();

            long startMemory = Runtime.getRuntime().totalMemory() -
                              Runtime.getRuntime().freeMemory();
            long startTime = System.currentTimeMillis();

            sim.run(100);  // Extended simulation
            for (int i = 0; i < 10; i++) {
                sim.simulateMachineFailure("M" + (i % 4 + 1), "Test");
            }

            long endTime = System.currentTimeMillis();
            long endMemory = Runtime.getRuntime().totalMemory() -
                            Runtime.getRuntime().freeMemory();

            System.out.println(type.name() + ":");
            System.out.println("  Time: " + (endTime - startTime) + "ms");
            System.out.println("  Memory: " + (endMemory - startMemory) + " bytes");

            sim.stop();
        }
    }
}
```

## Conclusion

These examples demonstrate the flexibility and extensibility of the RLRA implementation. You can:

- Run pre-made simulations
- Create custom test scenarios
- Extend classes for specialized behavior
- Monitor message flow and agent state
- Compare architecture performance
- Test individual components
- Profile system performance

For more details, refer to the source code documentation and ARCHITECTURES.md.
