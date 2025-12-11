/**
 * Utility class for logging FIPA-ACL style messages between agents.
 * Provides standardized ACL message logging for inter-agent communication.
 */
public class ACLMessageLogger {
    
    /**
     * Log an ACL message in FIPA format.
     * 
     * @param fromAgent       Sender agent ID
     * @param toAgent         Receiver agent ID
     * @param performative    Message type (REQUEST, INFORM, FAILURE, etc.)
     * @param content         Message payload/content
     */
    public static void logMessage(String fromAgent, String toAgent, 
                                   String performative, String content) {
        System.out.println("[ACL] FROM " + fromAgent + 
                          " TO " + toAgent + 
                          " TYPE " + performative + 
                          " CONTENT \"" + content + "\"");
    }
    
    /**
     * Log a reconfiguration request ACL message.
     */
    public static void logReconfigurationRequest(String fromAgent, String toAgent, String machine) {
        logMessage(fromAgent, toAgent, "RECONFIGURATION_REQUEST", 
                  "FAILURE " + machine);
    }
    
    /**
     * Log a reconfiguration plan ACL message.
     */
    public static void logReconfigurationPlan(String fromAgent, String toAgent, 
                                              String strategy, String machine) {
        logMessage(fromAgent, toAgent, "RECONFIGURATION_PLAN", 
                  "STRATEGY_" + strategy + " for " + machine);
    }
    
    /**
     * Log an inform ACL message.
     */
    public static void logInform(String fromAgent, String toAgent, String content) {
        logMessage(fromAgent, toAgent, "INFORM", content);
    }
    
    /**
     * Log a conflict report ACL message.
     */
    public static void logConflictReport(String fromAgent, String toAgent, 
                                         String machine, String reason) {
        logMessage(fromAgent, toAgent, "CONFLICT_REPORT", 
                  "Machine " + machine + " failure - " + reason);
    }
    
    /**
     * Log a conflict resolution ACL message.
     */
    public static void logConflictResolution(String fromAgent, String toAgent, String resolution) {
        logMessage(fromAgent, toAgent, "CONFLICT_RESOLUTION", resolution);
    }
}
