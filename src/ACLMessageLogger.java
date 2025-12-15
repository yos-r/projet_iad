/**
 * Classe utilitaire pour la journalisation standardisée des messages FIPA-ACL.
 *
 * Cette classe fournit des méthodes statiques pour logger les communications inter-agents
 * selon le standard FIPA-ACL (Foundation for Intelligent Physical Agents - Agent Communication Language).
 *
 * Format standardisé : [ACL] FROM <expéditeur> TO <destinataire> TYPE <performative> CONTENT "<contenu>"
 *
 * Avantages :
 * - Uniformisation des logs de communication
 * - Traçabilité complète des interactions
 * - Facilitation du débogage et de l'audit
 * - Conformité au standard FIPA
 *
 * @author Eya Ben El Kadhi
 */
public class ACLMessageLogger {

    /**
     * Journalise un message ACL au format FIPA standard
     *
     * @param fromAgent     Identifiant de l'agent expéditeur
     * @param toAgent       Identifiant de l'agent destinataire
     * @param performative  Type de message (performative FIPA : REQUEST, INFORM, PROPOSE, etc.)
     * @param content       Contenu du message
     */
    public static void logMessage(String fromAgent, String toAgent,
                                   String performative, String content) {
        System.out.println("[ACL] FROM " + fromAgent +
                          " TO " + toAgent +
                          " TYPE " + performative +
                          " CONTENT \"" + content + "\"");
    }

    /**
     * Journalise une requête de reconfiguration
     * Performative : RECONFIGURATION_REQUEST
     *
     * @param fromAgent Identifiant de l'agent expéditeur (typiquement un Monitor)
     * @param toAgent   Identifiant de l'agent destinataire (typiquement un RLRA)
     * @param machine   Identifiant de la machine concernée
     */
    public static void logReconfigurationRequest(String fromAgent, String toAgent, String machine) {
        logMessage(fromAgent, toAgent, "RECONFIGURATION_REQUEST",
                  "FAILURE " + machine);
    }

    /**
     * Journalise un plan de reconfiguration
     * Performative : RECONFIGURATION_PLAN
     *
     * @param fromAgent Identifiant de l'agent expéditeur (typiquement un RLRA)
     * @param toAgent   Identifiant de l'agent destinataire (typiquement un Monitor)
     * @param strategy  Stratégie de reconfiguration choisie
     * @param machine   Identifiant de la machine concernée
     */
    public static void logReconfigurationPlan(String fromAgent, String toAgent,
                                              String strategy, String machine) {
        logMessage(fromAgent, toAgent, "RECONFIGURATION_PLAN",
                  "STRATEGY_" + strategy + " for " + machine);
    }

    /**
     * Journalise un message d'information général
     * Performative : INFORM
     *
     * @param fromAgent Identifiant de l'agent expéditeur
     * @param toAgent   Identifiant de l'agent destinataire
     * @param content   Contenu informationnel
     */
    public static void logInform(String fromAgent, String toAgent, String content) {
        logMessage(fromAgent, toAgent, "INFORM", content);
    }

    /**
     * Journalise un rapport de conflit (utilisé en architecture distribuée)
     * Performative : CONFLICT_REPORT
     *
     * @param fromAgent Identifiant de l'agent expéditeur (typiquement un Coordinator)
     * @param toAgent   Identifiant de l'agent destinataire (typiquement le Supervisor)
     * @param machine   Identifiant de la machine en conflit
     * @param reason    Raison du conflit
     */
    public static void logConflictReport(String fromAgent, String toAgent,
                                         String machine, String reason) {
        logMessage(fromAgent, toAgent, "CONFLICT_REPORT",
                  "Machine " + machine + " failure - " + reason);
    }

    /**
     * Journalise une résolution de conflit (utilisé en architecture distribuée)
     * Performative : CONFLICT_RESOLUTION
     *
     * @param fromAgent  Identifiant de l'agent expéditeur (typiquement le Supervisor)
     * @param toAgent    Identifiant de l'agent destinataire (typiquement un Coordinator)
     * @param resolution Description de la résolution globale
     */
    public static void logConflictResolution(String fromAgent, String toAgent, String resolution) {
        logMessage(fromAgent, toAgent, "CONFLICT_RESOLUTION", resolution);
    }
}
