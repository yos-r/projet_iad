import java.util.*;

/**
 * Enum pour les types de scénarios de reconfiguration
 */
public enum ReconfigurationScenario {
    MACHINE_FAILURE("Machine failure or degradation"),
    PRODUCTION_PEAK("High demand urgent orders"),
    PRODUCT_CHANGE("Product type change"),
    BUFFER_CONGESTION("Transport buffer congestion"),
    TRANSPORT_FAILURE("Transport system failure");

    private String description;

    ReconfigurationScenario(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
