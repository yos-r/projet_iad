/**
 * Factory class to instantiate different RLRA architectures.
 */
public class RLRAFactory {
    public enum ArchitectureType {
        CENTRALIZED,
        MODULAR,
        DISTRIBUTED
    }

    /**
     * Creates an RLRA controller of the specified type.
     */
    public static BaseAgent createRLRA(ArchitectureType type, String id) {
        switch (type) {
            case CENTRALIZED:
                return new RLRACentralized(id);
            case MODULAR:
                return new RLRAModular(id);
            case DISTRIBUTED:
                return new RLRADistributed(id);
            default:
                throw new IllegalArgumentException("Unknown architecture type: " + type);
        }
    }

    /**
     * Creates a complete factory system with the specified RLRA architecture.
     */
    public static FactorySystem createFactorySystem(ArchitectureType type) {
        BaseAgent rlra = createRLRA(type, "RLRA_Main");
        return new FactorySystem(rlra, type);
    }

    /**
     * Helper class to encapsulate a complete factory system.
     */
    public static class FactorySystem {
        private BaseAgent rlra;
        private ArchitectureType architectureType;

        public FactorySystem(BaseAgent rlra, ArchitectureType type) {
            this.rlra = rlra;
            this.architectureType = type;
        }

        public BaseAgent getRLRA() {
            return rlra;
        }

        public ArchitectureType getArchitectureType() {
            return architectureType;
        }

        public String getArchitectureName() {
            switch (architectureType) {
                case CENTRALIZED:
                    return "Centralized";
                case MODULAR:
                    return "Modular (Composite)";
                case DISTRIBUTED:
                    return "Distributed";
                default:
                    return "Unknown";
            }
        }
    }
}
