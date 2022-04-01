package entity

/**
 * App状态实体
 */
class AppStatus implements Serializable {

    private String projectName
    private boolean isReadiness
    private List<PodStatus> podStatuses

    String getProjectName() {
        return projectName
    }

    void setProjectName(String projectName) {
        this.projectName = projectName
    }

    boolean isReadiness() {
        return isReadiness
    }

    void setIsReadiness(boolean isReadiness) {
        this.isReadiness = isReadiness
    }

    List<PodStatus> getPodStatuses() {
        return podStatuses
    }

    void setPodStatuses(List<PodStatus> podStatuses) {
        this.podStatuses = podStatuses
    }
}
