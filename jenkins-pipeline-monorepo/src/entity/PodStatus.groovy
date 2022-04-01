package entity

/**
 * Pod状态实体
 */
class PodStatus implements Serializable {

    private String name
    private String status
    private int totalContainers
    private int readyContainers

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getStatus() {
        return status
    }

    void setStatus(String status) {
        this.status = status
    }

    int getTotalContainers() {
        return totalContainers
    }

    void setTotalContainers(int totalContainers) {
        this.totalContainers = totalContainers
    }

    int getReadyContainers() {
        return readyContainers
    }

    void setReadyContainers(int readyContainers) {
        this.readyContainers = readyContainers
    }
}
