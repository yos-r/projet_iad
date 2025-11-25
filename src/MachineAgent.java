/**
 * Agent representing a single machine in the factory.
 */
public class MachineAgent extends BaseAgent {
    private MachineState state;
    private int defaultCycleTime;

    public MachineAgent(String machineId, int cycleTime) {
        super(machineId, "Machine");
        this.state = new MachineState(machineId);
        this.state.setCycleTime(cycleTime);
        this.defaultCycleTime = cycleTime;
    }

    public MachineState getState() {
        return state;
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
        try {
            executeAction(action);
            // Report success
            sendMessage(message.getSenderId(), Message.MessageType.ACTION_RESULT,
                       "Action " + action + " completed on " + state.getMachineId());
        } catch (Exception e) {
            state.setStatus(MachineState.Status.FAILED);
            state.setErrorMessage(e.getMessage());
            // Report failure
            sendMessage(message.getSenderId(), Message.MessageType.ACTION_RESULT,
                       "Action failed: " + e.getMessage());
        }
    }

    private void handleStateUpdate(Message message) {
        // Update local state based on message
        String updateInfo = (String) message.getPayload();
        System.out.println(state.getMachineId() + " received state update: " + updateInfo);
    }

    private void executeAction(String action) throws Exception {
        String[] parts = action.split(":");
        String command = parts[0].trim();

        switch (command) {
            case "PROCESS":
                simulateProcessing();
                break;
            case "BYPASS":
                state.setStatus(MachineState.Status.DISABLED);
                state.setErrorMessage("Machine bypassed");
                break;
            case "ACCELERATE":
                state.setCycleTime(Math.max(1, defaultCycleTime / 2));
                break;
            case "RESET":
                state.setStatus(MachineState.Status.OPERATIONAL);
                state.setCycleTime(defaultCycleTime);
                state.setErrorMessage(null);
                break;
            default:
                throw new Exception("Unknown action: " + command);
        }
    }

    private void simulateProcessing() {
        state.setStatus(MachineState.Status.PROCESSING);
        state.setUtilization(0.95);
        state.setItemsProcessed(state.getItemsProcessed() + 1);
        // Simulate processing time
        try {
            Thread.sleep(state.getCycleTime() * 100); // Simulated time
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        state.setStatus(MachineState.Status.OPERATIONAL);
    }

    /**
     * Simulates a machine failure for testing.
     */
    public void simulateFailure(String errorMessage) {
        state.setStatus(MachineState.Status.FAILED);
        state.setErrorMessage(errorMessage);
        state.setUtilization(0.0);
    }
}
