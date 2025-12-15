import java.util.*;

/**
 * Architecture RLRA Centralisée
 *
 * Un contrôleur unique prend toutes les décisions de reconfiguration.
 * Avantages : Simple, déterministe, vision globale
 * Inconvénients : Point unique de défaillance, goulot d'étranglement potentiel
 */
public class RLRACentralized extends BaseAgent {
    // Map des agents moniteurs enregistrés (un par site)
    private Map<String, MonitorAgent> monitors;

    // Map des agents machines enregistrés
    private Map<String, MachineAgent> machines;

    // Historique des reconfigurations effectuées
    private List<String> reconfigurationHistory;

    /**
     * Constructeur de l'agent RLRA centralisé
     * @param id Identifiant unique de l'agent
     */
    public RLRACentralized(String id) {
        super(id, "RLRA_Centralized");
        this.monitors = new HashMap<>();
        this.machines = new HashMap<>();
        this.reconfigurationHistory = new ArrayList<>();
    }

    /**
     * Enregistre un agent moniteur dans le système
     * @param monitor Agent moniteur à enregistrer
     */
    public void registerMonitor(MonitorAgent monitor) {
        monitors.put(monitor.getId(), monitor);
    }

    /**
     * Enregistre un agent machine dans le système
     * @param machine Agent machine à enregistrer
     */
    public void registerMachine(MachineAgent machine) {
        machines.put(machine.getId(), machine);
    }

    /**
     * Gère les messages reçus par l'agent RLRA
     * @param message Message à traiter
     */
    @Override
    protected void handleMessage(Message message) {
        switch (message.getType()) {
            case RECONFIGURATION_REQUEST:
                // Requête de reconfiguration d'un moniteur
                handleReconfigurationRequest(message);
                break;
            case ACTION_RESULT:
                // Résultat d'une action exécutée par une machine
                handleActionResult(message);
                break;
            default:
                break;
        }
    }

    /**
     * Traite une requête de reconfiguration
     * @param message Message contenant les détails de la requête
     */
    private void handleReconfigurationRequest(Message message) {
        Map<String, Object> request = (Map<String, Object>) message.getPayload();
        String affectedMachine = (String) request.get("affectedMachine");
        String issue = (String) request.get("issue");

        System.out.println("[" + id + "] Processing reconfiguration request for " + affectedMachine);

        // Décider de la stratégie de reconfiguration
        String strategy = decidePlan(affectedMachine, issue);

        // Exécuter le plan choisi
        executePlan(strategy, affectedMachine);

        // Notifier tous les moniteurs du plan
        notifyMonitors(strategy);
    }

    /**
     * Traite le résultat d'une action exécutée
     * @param message Message contenant le résultat
     */
    private void handleActionResult(Message message) {
        String result = (String) message.getPayload();
        System.out.println("[" + id + "] Machine reported: " + result);
    }

    /**
     * Logique de décision centralisée : analyse la situation et choisit la stratégie
     * @param affectedMachine Machine affectée par le problème
     * @param issue Description du problème
     * @return Stratégie de reconfiguration choisie
     */
    private String decidePlan(String affectedMachine, String issue) {
        MachineAgent machine = machines.get(affectedMachine);
        if (machine == null) {
            return "STRATEGY_UNKNOWN";
        }

        MachineState state = machine.getState();

        // Sélection de stratégie basée sur le type de problème
        if (issue.contains("fail")) {
            // Pour une panne machine, essayer réaffectation ou contournement
            if (hasBackupMachine(affectedMachine)) {
                return "STRATEGY_REASSIGNMENT";  // Réaffecter à une machine de secours
            } else {
                return "STRATEGY_BYPASS";  // Contourner la machine défaillante
            }
        } else if (issue.contains("slow")) {
            return "STRATEGY_ACCELERATION";  // Accélérer la machine lente
        } else {
            return "STRATEGY_BYPASS";  // Stratégie par défaut
        }
    }

    /**
     * Exécute le plan de reconfiguration choisi
     * @param strategy Stratégie à exécuter
     * @param affectedMachine Machine concernée
     */
    private void executePlan(String strategy, String affectedMachine) {
        switch (strategy) {
            case "STRATEGY_BYPASS":
                executionBypass(affectedMachine);
                break;
            case "STRATEGY_REASSIGNMENT":
                executeReassignment(affectedMachine);
                break;
            case "STRATEGY_ACCELERATION":
                executeAcceleration(affectedMachine);
                break;
            default:
                break;
        }
        // Enregistrer dans l'historique
        reconfigurationHistory.add(strategy + " for " + affectedMachine);
    }

    /**
     * Exécute la stratégie de contournement
     * @param affectedMachine Machine à contourner
     */
    private void executionBypass(String affectedMachine) {
        System.out.println("[" + id + "] Executing BYPASS strategy for " + affectedMachine);
        MachineAgent machine = machines.get(affectedMachine);
        if (machine != null) {
            sendMessage(affectedMachine, Message.MessageType.EXECUTE_ACTION, "BYPASS");
        }
    }

    /**
     * Exécute la stratégie de réaffectation
     * Trouve une machine de secours opérationnelle et lui réaffecte le travail
     * @param affectedMachine Machine défaillante
     */
    private void executeReassignment(String affectedMachine) {
        System.out.println("[" + id + "] Executing REASSIGNMENT strategy for " + affectedMachine);
        // Trouver une machine de secours
        for (String machineId : machines.keySet()) {
            MachineAgent machine = machines.get(machineId);
            if (!machineId.equals(affectedMachine) && machine.getState().getStatus() == MachineState.Status.OPERATIONAL) {
                System.out.println("[" + id + "] Reassigning work to " + machineId);
                break;
            }
        }
    }

    /**
     * Exécute la stratégie d'accélération
     * Augmente la vitesse de traitement de la machine
     * @param affectedMachine Machine à accélérer
     */
    private void executeAcceleration(String affectedMachine) {
        System.out.println("[" + id + "] Executing ACCELERATION strategy for " + affectedMachine);
        sendMessage(affectedMachine, Message.MessageType.EXECUTE_ACTION, "ACCELERATE");
    }

    /**
     * Vérifie s'il existe une machine de secours opérationnelle
     * @param affectedMachine Machine défaillante
     * @return true s'il existe une machine de secours, false sinon
     */
    private boolean hasBackupMachine(String affectedMachine) {
        for (String machineId : machines.keySet()) {
            MachineAgent machine = machines.get(machineId);
            if (!machineId.equals(affectedMachine) && machine.getState().getStatus() == MachineState.Status.OPERATIONAL) {
                return true;
            }
        }
        return false;
    }

    /**
     * Notifie tous les moniteurs du plan de reconfiguration
     * @param strategy Stratégie choisie
     */
    private void notifyMonitors(String strategy) {
        for (MonitorAgent monitor : monitors.values()) {
            sendMessage(monitor.getId(), Message.MessageType.RECONFIGURATION_PLAN,
                       "Plan: " + strategy);
        }
    }

    /**
     * Retourne l'historique des reconfigurations
     * @return Copie de la liste d'historique
     */
    public List<String> getReconfigurationHistory() {
        return new ArrayList<>(reconfigurationHistory);
    }
}
