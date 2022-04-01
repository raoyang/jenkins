package entity

class Context implements Serializable {

    // 操作jenkins方法
    private def jenkins
    // 当前分支名
    private String branchName

    // 起点版本
    private String fromVersion
    // 构建版本
    private String toVersion

    // 全局配置
    private GlobalConfig globalConfig
    // 项目配置
    private Map<String, ProjectItemConfig> projectsConfig

    Context(jenkins) {
        this.jenkins = jenkins
    }

    def getJenkins() {
        return jenkins
    }

    void setJenkins(jenkins) {
        this.jenkins = jenkins
    }

    String getBranchName() {
        return branchName
    }

    void setBranchName(String branchName) {
        this.branchName = branchName
    }

    String getFromVersion() {
        return this.fromVersion
    }

    void setFromVersion(String fromVersion) {
        this.fromVersion = fromVersion
    }

    String getToVersion() {
        return this.toVersion
    }

    void setToVersion(String toVersion) {
        this.toVersion = toVersion
    }

//    @NonCPS
    GlobalConfig getGlobalConfig() {
        return globalConfig
    }

//    @NonCPS
    void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig
    }

//    @NonCPS
    Map<String, ProjectItemConfig> getProjectsConfig() {
        return projectsConfig
    }

//    @NonCPS
    void setProjectsConfig(Map<String, ProjectItemConfig> projectsConfig) {
        this.projectsConfig = projectsConfig
    }
}
