package cmd.utils

/**
 * 校验相关工具
 */
class ValidateUtils {

    /**
     * 忽略配置
     * @param currentCluster
     * @param currentVirtualEnv
     * @param config
     * @return
     */
    static boolean ignore(String currentCluster, String currentVirtualEnv, def config) {
        boolean flag = false
        if (currentCluster != null && config["ignoreCluster"] != null) {
            flag = config["ignoreCluster"].contains(currentCluster)
        }
        if (currentVirtualEnv != null && config["ignoreVirtualEnv"] != null) {
            flag = config["ignoreVirtualEnv"].contains(currentVirtualEnv)
        }

        return flag
    }

}
