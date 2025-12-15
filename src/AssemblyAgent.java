import java.util.*;

/**
 * Agent d'Assemblage - Machine spécialisée qui combine plusieurs composants.
 *
 * Cet agent est plus sophistiqué que MachineAgent de base avec :
 * - Gestion des composants et de l'inventaire
 * - Support de plusieurs types de produits (Alpha, Beta, Gamma)
 * - Recalibration lors du changement de produit
 * - Gestion de la dégradation et des pénuries de composants
 */
public class AssemblyAgent extends BaseAgent {

    /**
     * Énumération des types de composants utilisés dans l'assemblage
     */
    public enum ComponentType {
        FRAME,      // Châssis
        MOTOR,      // Moteur
        HOUSING,    // Boîtier
        FASTENER,   // Attache/Vis
        SENSOR,     // Capteur
        WIRING      // Câblage
    }

    // État de la machine d'assemblage
    private MachineState machineState;

    // Temps de cycle par défaut (en secondes)
    private int defaultCycleTime;

    // Composants requis par assemblage (selon type de produit)
    private Map<ComponentType, Integer> requiredComponents;

    // Inventaire actuel des composants disponibles
    private Map<ComponentType, Integer> availableComponents;

    // File d'attente des assemblages
    private Queue<String> assemblyQueue;

    // Liste des assemblages complétés
    private List<String> completedAssemblies;

    // Type de produit actuellement assemblé (Alpha, Beta, Gamma)
    private String currentProduct;

    // Indicateur de besoin de recalibration
    private boolean requiresRecalibration;

    /**
     * Constructeur de l'agent d'assemblage
     * @param machineId Identifiant de la machine
     * @param cycleTime Temps de cycle en secondes
     * @param productType Type de produit à assembler (Alpha, Beta, Gamma)
     */
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
     * Initialise les exigences en composants selon le type de produit
     * @param productType Type de produit (Alpha, Beta, Gamma)
     */
    private void initializeComponentRequirements(String productType) {
        requiredComponents = new HashMap<>();
        availableComponents = new HashMap<>();

        if ("Alpha".equals(productType)) {
            // Assemblage simple : Châssis + Moteur + Boîtier + 4 Attaches
            requiredComponents.put(ComponentType.FRAME, 1);
            requiredComponents.put(ComponentType.MOTOR, 1);
            requiredComponents.put(ComponentType.HOUSING, 1);
            requiredComponents.put(ComponentType.FASTENER, 4);
        } else if ("Beta".equals(productType)) {
            // Assemblage complexe : Châssis + Moteur + Boîtier + Capteur + Câblage + 8 Attaches
            requiredComponents.put(ComponentType.FRAME, 1);
            requiredComponents.put(ComponentType.MOTOR, 1);
            requiredComponents.put(ComponentType.HOUSING, 1);
            requiredComponents.put(ComponentType.SENSOR, 1);
            requiredComponents.put(ComponentType.WIRING, 1);
            requiredComponents.put(ComponentType.FASTENER, 8);
        } else if ("Gamma".equals(productType)) {
            // Assemblage lourd : 2 Châssis + Moteur + Boîtier + 2 Capteurs + Câblage + 12 Attaches
            requiredComponents.put(ComponentType.FRAME, 2);
            requiredComponents.put(ComponentType.MOTOR, 1);
            requiredComponents.put(ComponentType.HOUSING, 1);
            requiredComponents.put(ComponentType.SENSOR, 2);
            requiredComponents.put(ComponentType.WIRING, 1);
            requiredComponents.put(ComponentType.FASTENER, 12);
        }

        // Initialiser l'inventaire à zéro pour tous les types de composants
        for (ComponentType type : ComponentType.values()) {
            availableComponents.put(type, 0);
        }
    }

    /**
     * Retourne l'état de la machine
     * @return État actuel de la machine
     */
    public MachineState getState() {
        return machineState;
    }

    /**
     * Retourne le type de produit actuellement assemblé
     * @return Type de produit (Alpha, Beta, Gamma)
     */
    public String getCurrentProduct() {
        return currentProduct;
    }

    /**
     * Approvisionne la machine en composants
     * @param type Type de composant
     * @param quantity Quantité à ajouter
     * @return true si l'approvisionnement a réussi, false sinon
     */
    public boolean supplyComponent(ComponentType type, int quantity) {
        if (quantity <= 0) return false;
        Integer current = availableComponents.getOrDefault(type, 0);
        availableComponents.put(type, current + quantity);
        System.out.println("[" + id + "] Received " + quantity + " x " + type);
        return true;
    }

    /**
     * Vérifie si l'assemblage peut procéder (tous les composants disponibles)
     * @return true si tous les composants requis sont disponibles, false sinon
     */
    public boolean canAssemble() {
        for (ComponentType type : requiredComponents.keySet()) {
            int required = requiredComponents.get(type);
            int available = availableComponents.getOrDefault(type, 0);
            if (available < required) {
                return false;  // Composant manquant
            }
        }
        return true;
    }

    /**
     * Retourne le statut de préparation à l'assemblage
     * @return Map contenant le produit, la disponibilité, le temps de cycle et les composants
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
     * Effectue une opération d'assemblage
     * Consomme les composants requis et produit un assemblage
     * @return ID de l'assemblage complété ou null si impossible
     */
    public String performAssembly() {
        if (!canAssemble()) {
            machineState.setStatus(MachineState.Status.DEGRADED);
            return null; // Impossible d'assembler : composants manquants
        }

        // Vérifier si recalibration nécessaire pour changement de produit
        if (requiresRecalibration) {
            System.out.println("[" + id + "] Recalibration in progress for " + currentProduct);
            requiresRecalibration = false;
            // Ralentissement de 50% pendant la recalibration
            machineState.setCycleTime((int) (defaultCycleTime * 1.5));
            return null;
        }

        machineState.setStatus(MachineState.Status.PROCESSING);

        // Consommer les composants requis
        for (ComponentType type : requiredComponents.keySet()) {
            int required = requiredComponents.get(type);
            int available = availableComponents.get(type);
            availableComponents.put(type, available - required);
        }

        // Simuler le temps d'assemblage
        String assemblyId = id + "_" + System.currentTimeMillis();
        try {
            Thread.sleep(machineState.getCycleTime() * 50); // Temps simulé
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Enregistrer l'assemblage complété
        completedAssemblies.add(assemblyId);
        machineState.setStatus(MachineState.Status.OPERATIONAL);
        machineState.setItemsProcessed(machineState.getItemsProcessed() + 1);

        System.out.println("[" + id + "] Completed assembly: " + assemblyId + " (Product: " + currentProduct + ")");
        return assemblyId;
    }

    /**
     * Change le type de produit (nécessite une recalibration)
     * @param newProductType Nouveau type de produit (Alpha, Beta, Gamma)
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

    /**
     * Gère les messages reçus par l'agent
     * @param message Message à traiter
     */
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

    /**
     * Traite les messages de type EXECUTE_ACTION
     * Supporte les actions : SUPPLY_COMPONENT, PERFORM_ASSEMBLY, CHANGE_PRODUCT, RECALIBRATE, RESET
     * @param message Message contenant l'action à exécuter
     */
    private void handleExecuteAction(Message message) {
        String action = (String) message.getPayload();
        String[] parts = action.split(":");

        switch (parts[0].trim()) {
            case "SUPPLY_COMPONENT":
                // Format : "SUPPLY_COMPONENT:TYPE:QUANTITY"
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
                // Format : "CHANGE_PRODUCT:ProductType"
                if (parts.length > 1) {
                    changeProduct(parts[1].trim());
                }
                break;
            case "RECALIBRATE":
                requiresRecalibration = true;
                System.out.println("[" + id + "] Manual recalibration requested");
                break;
            case "RESET":
                // Réinitialiser la machine à l'état opérationnel
                machineState.setStatus(MachineState.Status.OPERATIONAL);
                machineState.setCycleTime(defaultCycleTime);
                requiresRecalibration = false;
                System.out.println("[" + id + "] Reset complete");
                break;
        }
    }

    /**
     * Traite les messages de type STATE_UPDATE
     * @param message Message de mise à jour d'état
     */
    private void handleStateUpdate(Message message) {
        // Gestion des mises à jour d'état
    }

    /**
     * Simule une dégradation de l'équipement
     * Augmente le temps de cycle de 30%
     */
    public void simulateDegradation() {
        machineState.setStatus(MachineState.Status.DEGRADED);
        machineState.setCycleTime((int) (machineState.getCycleTime() * 1.3));
        System.out.println("[" + id + "] Equipment degraded, cycle time increased");
    }

    /**
     * Simule une pénurie de composants
     * @param type Type de composant en pénurie
     * @param quantity Quantité perdue
     */
    public void simulateComponentShortage(ComponentType type, int quantity) {
        Integer current = availableComponents.get(type);
        if (current != null && current >= quantity) {
            availableComponents.put(type, current - quantity);
        }
        System.out.println("[" + id + "] Component shortage: " + type + " (lost " + quantity + ")");
    }

    /**
     * Retourne les statistiques de l'agent d'assemblage
     * @return Chaîne formatée avec les statistiques
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

    /**
     * Retourne la liste des assemblages complétés
     * @return Copie de la liste des IDs d'assemblages complétés
     */
    public List<String> getCompletedAssemblies() {
        return new ArrayList<>(completedAssemblies);
    }

    /**
     * Exécute un pas de simulation
     * Peut automatiquement effectuer un assemblage si les composants sont disponibles
     */
    @Override
    public void step() {
        super.step();
        if (canAssemble() && machineState.getStatus() == MachineState.Status.OPERATIONAL) {
            // Possibilité d'effectuer automatiquement l'assemblage si composants disponibles
            // performAssembly();
        }
    }
}
