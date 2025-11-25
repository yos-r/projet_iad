import java.util.*;

/**
 * AssemblyAgent represents a specialized assembly machine that combines multiple components.
 * More sophisticated than basic MachineAgent with component management.
 */
public class AssemblyAgent extends BaseAgent {
    public enum ComponentType {
        FRAME, MOTOR, HOUSING, FASTENER, SENSOR, WIRING
    }

    private MachineState machineState;
    private int defaultCycleTime;
    private Map<ComponentType, Integer> requiredComponents; // parts per assembly
    private Map<ComponentType, Integer> availableComponents; // current inventory
    private Queue<String> assemblyQueue;
    private List<String> completedAssemblies;
    private String currentProduct; // current product type being assembled
    private boolean requiresRecalibration;

    public AssemblyAgent(String machineId, int cycleTime, String productType) {
        super(machineId, "Assembly");
        this.machineState = new MachineState(machineId);
        this.machineState.setCycleTime(cycleTime);
        this.defaultCycleTime = cycleTime;
        this.currentProduct = productType;
        this.assemblyQueue = new LinkedList<>();
        this.completedAssemblies = new ArrayList<>();
        this.requiresRecalibration = false;
        initializeComponentRequirements(productType);
    }

    /**
     * Set component requirements based on product type
     */
    private void initializeComponentRequirements(String productType) {
        requiredComponents = new HashMap<>();
        availableComponents = new HashMap<>();

        if ("Alpha".equals(productType)) {
            // Simple assembly: Frame + Motor + Housing + 4x Fasteners
            requiredComponents.put(ComponentType.FRAME, 1);
            requiredComponents.put(ComponentType.MOTOR, 1);
            requiredComponents.put(ComponentType.HOUSING, 1);
            requiredComponents.put(ComponentType.FASTENER, 4);
        } else if ("Beta".equals(productType)) {
            // Complex assembly: Frame + Motor + Housing + Sensor + Wiring + 8x Fasteners
            requiredComponents.put(ComponentType.FRAME, 1);
            requiredComponents.put(ComponentType.MOTOR, 1);
            requiredComponents.put(ComponentType.HOUSING, 1);
            requiredComponents.put(ComponentType.SENSOR, 1);
            requiredComponents.put(ComponentType.WIRING, 1);
            requiredComponents.put(ComponentType.FASTENER, 8);
        } else if ("Gamma".equals(productType)) {
            // Heavy assembly: Frame x2 + Motor + Housing + Sensor x2 + Wiring + 12x Fasteners
            requiredComponents.put(ComponentType.FRAME, 2);
            requiredComponents.put(ComponentType.MOTOR, 1);
            requiredComponents.put(ComponentType.HOUSING, 1);
            requiredComponents.put(ComponentType.SENSOR, 2);
            requiredComponents.put(ComponentType.WIRING, 1);
            requiredComponents.put(ComponentType.FASTENER, 12);
        }

        // Initialize inventory to zero
        for (ComponentType type : ComponentType.values()) {
            availableComponents.put(type, 0);
        }
    }

    public MachineState getState() {
        return machineState;
    }

    public String getCurrentProduct() {
        return currentProduct;
    }

    /**
     * Supply components to the assembly machine
     */
    public boolean supplyComponent(ComponentType type, int quantity) {
        if (quantity <= 0) return false;
        Integer current = availableComponents.getOrDefault(type, 0);
        availableComponents.put(type, current + quantity);
        System.out.println("[" + id + "] Received " + quantity + " x " + type);
        return true;
    }

    /**
     * Check if assembly can proceed (all components available)
     */
    public boolean canAssemble() {
        for (ComponentType type : requiredComponents.keySet()) {
            int required = requiredComponents.get(type);
            int available = availableComponents.getOrDefault(type, 0);
            if (available < required) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get assembly readiness status
     */
    public Map<String, Object> getAssemblyStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("product", currentProduct);
        status.put("canAssemble", canAssemble());
        status.put("cycleTime", machineState.getCycleTime());

        Map<String, Object> components = new HashMap<>();
        for (ComponentType type : requiredComponents.keySet()) {
            String key = type.toString();
            int required = requiredComponents.get(type);
            int available = availableComponents.getOrDefault(type, 0);
            components.put(key, available + "/" + required);
        }
        status.put("components", components);

        return status;
    }

    /**
     * Perform assembly operation
     */
    public String performAssembly() {
        if (!canAssemble()) {
            machineState.setStatus(MachineState.Status.DEGRADED);
            return null; // Cannot assemble
        }

        // Check if recalibration needed for product change
        if (requiresRecalibration) {
            System.out.println("[" + id + "] Recalibration in progress for " + currentProduct);
            requiresRecalibration = false;
            machineState.setCycleTime((int) (defaultCycleTime * 1.5)); // 50% slower during recalibration
            return null;
        }

        machineState.setStatus(MachineState.Status.PROCESSING);

        // Consume components
        for (ComponentType type : requiredComponents.keySet()) {
            int required = requiredComponents.get(type);
            int available = availableComponents.get(type);
            availableComponents.put(type, available - required);
        }

        // Simulate assembly time
        String assemblyId = id + "_" + System.currentTimeMillis();
        try {
            Thread.sleep(machineState.getCycleTime() * 50); // Simulated time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        completedAssemblies.add(assemblyId);
        machineState.setStatus(MachineState.Status.OPERATIONAL);
        machineState.setItemsProcessed(machineState.getItemsProcessed() + 1);

        System.out.println("[" + id + "] Completed assembly: " + assemblyId + " (Product: " + currentProduct + ")");
        return assemblyId;
    }

    /**
     * Change product type (requires recalibration)
     */
    public void changeProduct(String newProductType) {
        if (!newProductType.equals(currentProduct)) {
            System.out.println("[" + id + "] Product change requested: " + currentProduct + " → " + newProductType);
            currentProduct = newProductType;
            initializeComponentRequirements(newProductType);
            requiresRecalibration = true;
            machineState.setCurrentProduct(newProductType);
            System.out.println("[" + id + "] Recalibration required for " + newProductType);
        }
    }

    @Override
    protected void handleMessage(Message message) {
        switch (message.getType()) {
            case EXECUTE_ACTION:
                handleExecuteAction(message);
                break;
            case STATE_UPDATE:
                handleStateUpdate(message);
                break;
            default:
                break;
        }
    }

    private void handleExecuteAction(Message message) {
        String action = (String) message.getPayload();
        String[] parts = action.split(":");

        switch (parts[0].trim()) {
            case "SUPPLY_COMPONENT":
                if (parts.length >= 3) {
                    try {
                        ComponentType type = ComponentType.valueOf(parts[1].trim());
                        int quantity = Integer.parseInt(parts[2].trim());
                        supplyComponent(type, quantity);
                    } catch (Exception e) {
                        System.out.println("[" + id + "] Invalid component supply: " + e.getMessage());
                    }
                }
                break;
            case "PERFORM_ASSEMBLY":
                performAssembly();
                break;
            case "CHANGE_PRODUCT":
                if (parts.length > 1) {
                    changeProduct(parts[1].trim());
                }
                break;
            case "RECALIBRATE":
                requiresRecalibration = true;
                System.out.println("[" + id + "] Manual recalibration requested");
                break;
            case "RESET":
                machineState.setStatus(MachineState.Status.OPERATIONAL);
                machineState.setCycleTime(defaultCycleTime);
                requiresRecalibration = false;
                System.out.println("[" + id + "] Reset complete");
                break;
        }
    }

    private void handleStateUpdate(Message message) {
        // Handle state updates
    }

    /**
     * Simulate equipment degradation
     */
    public void simulateDegradation() {
        machineState.setStatus(MachineState.Status.DEGRADED);
        machineState.setCycleTime((int) (machineState.getCycleTime() * 1.3));
        System.out.println("[" + id + "] Equipment degraded, cycle time increased");
    }

    /**
     * Simulate component shortage
     */
    public void simulateComponentShortage(ComponentType type, int quantity) {
        Integer current = availableComponents.get(type);
        if (current != null && current >= quantity) {
            availableComponents.put(type, current - quantity);
        }
        System.out.println("[" + id + "] Component shortage: " + type + " (lost " + quantity + ")");
    }

    /**
     * Get assembly statistics
     */
    public String getStatistics() {
        return String.format(
            "AssemblyAgent{id='%s', product='%s', status=%s, " +
            "completed=%d, cycleTime=%ds, requiresRecalibration=%s}",
            id, currentProduct, machineState.getStatus(),
            completedAssemblies.size(), machineState.getCycleTime(),
            requiresRecalibration
        );
    }

    public List<String> getCompletedAssemblies() {
        return new ArrayList<>(completedAssemblies);
    }

    @Override
    public void step() {
        super.step();
        if (canAssemble() && machineState.getStatus() == MachineState.Status.OPERATIONAL) {
            // Automatically perform assembly if components available
            // performAssembly();
        }
    }
}
