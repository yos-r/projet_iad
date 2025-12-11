/**
 * ACL Logger - FIPA-ACL message logging utility
 */
public class ACLLogger {
    
    public enum Performative {
        REQUEST("REQUEST"),
        INFORM("INFORM"),
        FAILURE("FAILURE"),
        PROPOSE("PROPOSE"),
        CONFLICT_RESOLUTION("CONFLICT_RESOLUTION");
        
        private final String value;
        Performative(String value) { this.value = value; }
        public String getValue() { return value; }
    }
    
    public static void logMessage(String sender, String receiver, 
                                   Performative performative, String content) {
        System.out.println("[ACL] FROM " + sender + " TO " + receiver + 
                           " TYPE " + performative.getValue() + 
                           " CONTENT '" + content + "'");
    }
    
    public static void logRequest(String sender, String receiver, String content) {
        logMessage(sender, receiver, Performative.REQUEST, content);
    }
    
    public static void logInform(String sender, String receiver, String content) {
        logMessage(sender, receiver, Performative.INFORM, content);
    }
    
    public static void logFailure(String sender, String receiver, String content) {
        logMessage(sender, receiver, Performative.FAILURE, content);
    }
    
    public static void logConflictResolution(String sender, String receiver, String resolution) {
        logMessage(sender, receiver, Performative.CONFLICT_RESOLUTION, resolution);
    }
}
