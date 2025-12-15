import java.util.*;
import java.util.concurrent.*;

/**
 * Courtier de messages central pour la communication inter-agents.
 *
 * Implémente le pattern Singleton pour garantir une instance unique.
 * Gère le routage des messages entre agents via des boîtes aux lettres (mailboxes).
 *
 * Fonctionnalités :
 * - Enregistrement/désenregistrement d'agents
 * - Envoi et réception de messages
 * - Journalisation des messages pour audit
 * - Statistiques de communication
 * - Redémarrage pour simulations multiples
 */
public class MessageBroker {
    // Instance unique du Singleton
    private static final MessageBroker instance = new MessageBroker();

    // Map des boîtes aux lettres par identifiant d'agent
    private Map<String, Queue<Message>> mailboxes;

    // Journal de tous les messages échangés
    private List<Message> messageLog;

    // État du broker (actif/inactif)
    private boolean running;

    /**
     * Constructeur privé (pattern Singleton)
     */
    private MessageBroker() {
        this.mailboxes = new ConcurrentHashMap<>();
        this.messageLog = Collections.synchronizedList(new ArrayList<>());
        this.running = true;
    }

    /**
     * Retourne l'instance unique du MessageBroker
     * @return Instance du MessageBroker
     */
    public static MessageBroker getInstance() {
        return instance;
    }

    /**
     * Enregistre un agent auprès du courtier de messages
     * Crée une boîte aux lettres pour l'agent
     * @param agentId Identifiant de l'agent
     */
    public void registerAgent(String agentId) {
        mailboxes.putIfAbsent(agentId, new ConcurrentLinkedQueue<>());
    }

    /**
     * Désenregistre un agent du courtier de messages
     * Supprime sa boîte aux lettres
     * @param agentId Identifiant de l'agent
     */
    public void unregisterAgent(String agentId) {
        mailboxes.remove(agentId);
    }

    /**
     * Envoie un message d'un agent à un autre
     * Ajoute le message à la boîte aux lettres du destinataire
     * @param message Message à envoyer
     */
    public void sendMessage(Message message) {
        if (!running) return;  // Ne pas envoyer si le broker est arrêté

        Queue<Message> mailbox = mailboxes.get(message.getReceiverId());
        if (mailbox != null) {
            mailbox.offer(message);  // Ajouter à la file
            messageLog.add(message);  // Enregistrer dans le journal
        }
    }

    /**
     * Récupère et retire le prochain message pour un agent
     * @param agentId Identifiant de l'agent
     * @return Message ou null si aucun message
     */
    public Message getMessage(String agentId) {
        Queue<Message> mailbox = mailboxes.get(agentId);
        if (mailbox != null) {
            return mailbox.poll();  // Retirer et retourner le premier message
        }
        return null;
    }

    /**
     * Vérifie si un agent a des messages en attente
     * @param agentId Identifiant de l'agent
     * @return true si l'agent a des messages, false sinon
     */
    public boolean hasMessages(String agentId) {
        Queue<Message> mailbox = mailboxes.get(agentId);
        return mailbox != null && !mailbox.isEmpty();
    }

    /**
     * Consulte tous les messages d'un agent sans les retirer
     * @param agentId Identifiant de l'agent
     * @return Liste des messages en attente
     */
    public List<Message> peekMessages(String agentId) {
        Queue<Message> mailbox = mailboxes.get(agentId);
        if (mailbox != null) {
            return new ArrayList<>(mailbox);
        }
        return new ArrayList<>();
    }

    /**
     * Retourne le journal complet des messages pour débogage
     * @return Liste de tous les messages échangés
     */
    public List<Message> getMessageLog() {
        return new ArrayList<>(messageLog);
    }

    /**
     * Efface le journal des messages
     */
    public void clearMessageLog() {
        messageLog.clear();
    }

    /**
     * Arrête le courtier de messages
     * Efface toutes les boîtes aux lettres
     */
    public void stop() {
        running = false;
        mailboxes.clear();
    }

    /**
     * Redémarre le courtier de messages pour une nouvelle simulation
     * IMPORTANT : Cette méthode résout le problème de réutilisation
     * du Singleton entre plusieurs simulations successives
     */
    public void restart() {
        running = true;            // Réactiver le broker
        mailboxes.clear();         // Effacer les boîtes aux lettres
        messageLog.clear();        // Effacer le journal
    }

    /**
     * Affiche les statistiques de messages
     * Nombre total de messages, agents actifs, répartition par type
     */
    public void printStatistics() {
        System.out.println("=== Message Broker Statistics ===");
        System.out.println("Total messages logged: " + messageLog.size());
        System.out.println("Active agents: " + mailboxes.size());

        // Grouper les messages par type et afficher les comptes
        messageLog.stream()
                .collect(java.util.stream.Collectors.groupingBy(Message::getType, java.util.stream.Collectors.counting()))
                .forEach((type, count) -> System.out.println("  " + type + ": " + count));
    }
}
