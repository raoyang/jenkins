package entity.middleware

/**
 * 基础校验结果
 */
class BaseValidateResult {

    private String clusterName
    private String virtualEnv

    private Boolean allSuccessed

    /**
     * map: key=item关键字, value=异常信息
     */
    private def failedItems

    /**
     * 输出格式化字符
     *
     * @return
     */
    String formatFailed() {
        String format = "clusterName=${this.clusterName}\nvirtualEnv=${this.virtualEnv}\n"
        if (this.failedItems != null) {
            for (def item in this.failedItems) {
                format += "${item.key}=${item.value}\n"
            }
        }

        return format
    }

    Boolean getAllSuccessed() {
        return allSuccessed
    }

    void setAllSuccessed(Boolean allSuccessed) {
        this.allSuccessed = allSuccessed
    }

    def getFailedItems() {
        return failedItems
    }

    void setFailedItems(failedItems) {
        this.failedItems = failedItems
    }

    String getClusterName() {
        return clusterName
    }

    void setClusterName(String clusterName) {
        this.clusterName = clusterName
    }

    String getVirtualEnv() {
        return virtualEnv
    }

    void setVirtualEnv(String virtualEnv) {
        this.virtualEnv = virtualEnv
    }
}
