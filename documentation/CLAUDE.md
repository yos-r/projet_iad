# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java-based multi-agent system project for modeling a **FESTO CP Factory** - an intelligent cyber-physical manufacturing system with dynamic reconfiguration capabilities. The system coordinates multiple manufacturing sites, machines, and transport mechanisms to handle various production scenarios and anomalies.

### Key Concepts

**Factory Architecture:**
- **Site A**: Distribution (M1) and Machining (M2)
- **Transport**: Conveyor system (T1) between sites
- **Site B**: Assembly (M3) and Quality Control (M4)
- Standard production flow: M1 → M2 → Transport → M3 → M4

**Multi-Agent System:**
- **RLRA (Reconfiguration Logic Reasoning Agent)**: Central controller that makes reconfiguration decisions
- **Monitor Agents**: Per-site surveillance agents that detect failures and request reconfigurations
- **Machine Agents**: Per-machine agents that execute instructions and report status

**Reconfiguration Scenarios:**
1. Machine failure - bypass, reassign to backup, or adapt product
2. Production peak - acceleration, parallelization, or flow reorganization
3. Product change - reprogramming and adaptation of machines
4. Custom scenarios to be implemented

## Build, Compile, and Run

### Compilation
```bash
# Using VS Code Java extension
# The extension will automatically compile src/*.java to bin/

# Or manually compile with javac
javac -d bin src/App.java
```

### Running
```bash
# Using VS Code Java extension
# Run > Run Java

# Or from command line
java -cp bin App
```

### Configuration
- **Source directory**: `src/`
- **Output directory**: `bin/`
- **Library directory**: `lib/` (for .jar dependencies)
- See `.vscode/settings.json` for project configuration details

## Architecture Guidance

### Current State
The project is in early stages with a basic Java application structure. The specification (`promp.md`) outlines three architectural approaches for the RLRA controller:

1. **Centralized**: Single decision-making point at RLRA
2. **Modular (Composed)**: RLRA divided into Monitor, Learner, and Executor modules
3. **Distributed**: Per-site Coordinator and global Supervisor

### Implementation Approach
When implementing the multi-agent system:
- Start with agent base classes/interfaces defining common behaviors
- Implement RLRA as the central decision-making controller
- Create Monitor and Machine agent types with message-passing capabilities
- Use ACL (Agent Communication Language) protocol for inter-agent communication
- Design a conflict resolution mechanism for inter-site coordination
- Implement the reconfiguration scenarios incrementally

### Key Design Considerations
- **Message Protocol**: Define clear message formats for agent communication
- **State Management**: Track machine states, production orders, and reconfiguration plans
- **Decision Logic**: Implement optimization for time and energy in reconfiguration planning
- **Scalability**: Design so agents can be dynamically added/removed

## Project Structure

```
src/              # Java source files
├── App.java      # Entry point (current)
lib/              # External libraries (.jar files)
bin/              # Compiled output (generated)
.vscode/          # VS Code workspace configuration
```

## Requirements from Specification (promp.md)

The project must implement:

1. **Three RLRA controller architectures** - versions for centralized, modular, and distributed approaches
2. **Multi-agent architecture definition** - specify agent types, attributes, behaviors, knowledge, and messages
3. **Inter-agent interaction protocol** - ACL message format, scenarios, and conflict resolution
4. **Extended agents** - beyond basic Machine/Monitor types (e.g., Assembly agent, Part collector)
5. **Additional reconfiguration scenarios** - beyond the 3 provided in specification

## Development Notes

- The project uses standard Java without external frameworks initially
- When adding dependencies, place .jar files in `lib/` directory
- Consider using a multi-agent framework (like JADE) for advanced scenarios if needed
- The system should be testable with simulated scenarios before deployment
