/**
 * Represents the state of a machine in the factory.
 */
public class MachineState {
    public enum Status {
        OPERATIONAL,
        DEGRADED,
        FAILED,
        DISABLED,
        PROCESSING
    }

    private String machineId;
    private Status status;
    private int cycleTime; // in seconds
    private double utilization; // 0.0 to 1.0
    private String currentProduct;
    private int itemsProcessed;
    private String errorMessage;
    private long lastUpdate;

    public MachineState(String machineId) {
        this.machineId = machineId;
        this.status = Status.OPERATIONAL;
        this.utilization = 0.0;
        this.itemsProcessed = 0;
        this.lastUpdate = System.currentTimeMillis();
    }

    public String getMachineId() {
        return machineId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.lastUpdate = System.currentTimeMillis();
    }

    public int getCycleTime() {
        return cycleTime;
    }

    public void setCycleTime(int cycleTime) {
        this.cycleTime = cycleTime;
    }

    public double getUtilization() {
        return utilization;
    }

    public void setUtilization(double utilization) {
        this.utilization = utilization;
    }

    public String getCurrentProduct() {
        return currentProduct;
    }

    public void setCurrentProduct(String currentProduct) {
        this.currentProduct = currentProduct;
    }

    public int getItemsProcessed() {
        return itemsProcessed;
    }

    public void setItemsProcessed(int itemsProcessed) {
        this.itemsProcessed = itemsProcessed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public String toString() {
        return "MachineState{" +
                "machineId='" + machineId + '\'' +
                ", status=" + status +
                ", cycleTime=" + cycleTime +
                ", utilization=" + utilization +
                ", currentProduct='" + currentProduct + '\'' +
                ", itemsProcessed=" + itemsProcessed +
                ", errorMessage='" + errorMessage + '\'' +
                ", lastUpdate=" + lastUpdate +
                '}';
    }
}
