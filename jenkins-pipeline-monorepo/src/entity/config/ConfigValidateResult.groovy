package entity.config

class ConfigValidateResult implements Serializable {

    private String clusterName
    private String projectName
    private String virtualEnv

    /**
     * 是否所有配置校验成功
     */
    private Boolean allSuccessed

    /**
     * 校验失败的key
     */
    private def globalFailedItems
    private def virtualFailedItems
    private def cmdbFailedItems

    private def globalConfig
    private def virtualEnvConfig
    private def cmdbConfig

    /**
     * 输出格式化字符
     *
     * @return
     */
    String formatFailed() {
        String format = "clusterName=${this.clusterName}\nvirtualEnv=${this.virtualEnv}\nprojectName=${this.projectName}\n"
        if (this.globalFailedItems != null) {
            def configMap = [:]
            for (def itemConfig in this.globalConfig) {
                configMap[itemConfig["key"]] = itemConfig
            }

            for (def item in this.globalFailedItems) {
                format += "globalFailedItems=${item}, validateType=${configMap[item]["validateType"]}, validateRule=${configMap[item]["validateRule"] != null ? configMap[item]["validateRule"] : "无配置"}, description=${configMap[item]["description"]}\n"
            }
        }
        if (this.virtualFailedItems != null) {
            def configMap = [:]
            for (def itemConfig in this.virtualEnvConfig) {
                configMap[itemConfig["key"]] = itemConfig
            }

            for (def item in this.virtualFailedItems) {
                format += "virtualEnvFailedItems=${item}, validateType=${configMap[item]["validateType"]}, validateRule=${configMap[item]["validateRule"] != null ? configMap[item]["validateRule"] : "无配置"}, description=${configMap[item]["description"]}\n"
            }
        }
        if (this.cmdbFailedItems != null) {
            def configMap = [:]
            for (def itemConfig in this.cmdbConfig) {
                configMap[itemConfig["key"]] = itemConfig
            }

            for (def item in this.cmdbFailedItems) {
                format += "cmdbFailedItems=${item}, validateType=${configMap[item]["validateType"]}, validateRule=${configMap[item]["validateRule"] != null ? configMap[item]["validateRule"] : "无配置"}, description=${configMap[item]["description"]}\n"
            }
        }

        return format
    }

    Boolean getAllSuccessed() {
        return this.allSuccessed
    }

    void setAllSuccessed(Boolean allSuccessed) {
        this.allSuccessed = allSuccessed
    }

    def getGlobalFailedItems() {
        return globalFailedItems
    }

    void setGlobalFailedItems(globalFailedItems) {
        this.globalFailedItems = globalFailedItems
    }

    def getVirtualFailedItems() {
        return virtualFailedItems
    }

    void setVirtualFailedItems(virtualFailedItems) {
        this.virtualFailedItems = virtualFailedItems
    }

    def getCmdbFailedItems() {
        return cmdbFailedItems
    }

    void setCmdbFailedItems(cmdbFailedItems) {
        this.cmdbFailedItems = cmdbFailedItems
    }

    String getClusterName() {
        return clusterName
    }

    void setClusterName(String clusterName) {
        this.clusterName = clusterName
    }

    String getProjectName() {
        return projectName
    }

    void setProjectName(String projectName) {
        this.projectName = projectName
    }

    String getVirtualEnv() {
        return virtualEnv
    }

    void setVirtualEnv(String virtualEnv) {
        this.virtualEnv = virtualEnv
    }

    def getGlobalConfig() {
        return globalConfig
    }

    void setGlobalConfig(globalConfig) {
        this.globalConfig = globalConfig
    }

    def getVirtualEnvConfig() {
        return virtualEnvConfig
    }

    void setVirtualEnvConfig(virtualEnvConfig) {
        this.virtualEnvConfig = virtualEnvConfig
    }

    def getCmdbConfig() {
        return cmdbConfig
    }

    void setCmdbConfig(cmdbConfig) {
        this.cmdbConfig = cmdbConfig
    }
}
