package cmd

import cmd.utils.FrameworkApiUtils
import cmd.utils.ValidateUtils
import entity.config.ConfigValidateResult

class ConfigValidateCmd extends AbstractCmd<ConfigValidateResult> {

    private String clusterName
    private String projectName
    private String virtualEnv
    private def globalConfig
    private def virtualEnvConfig
    private def cmdbConfig

    private ConfigValidateResult resultImpl

    ConfigValidateCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("clusterName", this.clusterName)
        checkEmpty("projectName", this.projectName)

        ConfigValidateResult configValidateResult = new ConfigValidateResult()

        boolean isAllSuccessed = false
        // globalConfig检查
        if (this.globalConfig != null && this.globalConfig.size() > 0) {
            def configs = []
            for (def item in this.globalConfig) {
                checkEmpty("globalConfig.key", item["key"])
                checkEmpty("globalConfig.validateType, key=" + item["key"], item.validateType)

                if (ValidateUtils.ignore(this.clusterName, this.virtualEnv, item)) {
                    this.context.jenkins.echo "globalConfig-忽略配置检查：item=${item["key"]}, cluster=${this.clusterName}, virtualEnv=${this.virtualEnv}"
                } else {
                    configs.add(item)
                }
            }

            def apiResult = new LateinosFrameworkApiCmd(this.context)
                    .setClusterName(this.clusterName)
                    .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                    .setApiMethod("POST")
                    .setApiPath("/v1/configuration/validate/global")
                    .setApiBody([validateOptions: configs])
                    .execute().getResult()
            this.context.jenkins.echo "apiResult = ${apiResult}"

            isAllSuccessed = apiResult["data"]["allSuccessed"] != null ? apiResult["data"]["allSuccessed"] : false
            configValidateResult.globalFailedItems = apiResult["data"]["failedItems"]
        }

        // virtualEnvConfig检查
        if (this.virtualEnvConfig != null && this.virtualEnvConfig.size() > 0) {
            def configs = []
            for (def item in this.virtualEnvConfig) {
                checkEmpty("virtualEnvConfig.key", item["key"])
                checkEmpty("virtualEnvConfig.validateType, key=" + item["key"], item.validateType)

                if (ValidateUtils.ignore(this.clusterName, this.virtualEnv, item)) {
                    this.context.jenkins.echo "virtualEnvConfig-忽略配置检查：item=${item["key"]}, cluster=${this.clusterName}, virtualEnv=${this.virtualEnv}"
                } else {
                    configs.add(item)
                }
            }

            if (configs.size() > 0) {
                def apiResult = new LateinosFrameworkApiCmd(this.context)
                        .setClusterName(this.clusterName)
                        .setVirtualEnv(this.virtualEnv)
                        .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                        .setApiMethod("POST")
                        .setApiPath("/v1/configuration/validate/vfs")
                        .setApiBody([validateOptions: configs, vfs: this.virtualEnv])
                        .execute().getResult()
                this.context.jenkins.echo "apiResult = ${apiResult}"

                isAllSuccessed = apiResult["data"]["allSuccessed"] != null ? apiResult["data"]["allSuccessed"] : false
                configValidateResult.virtualFailedItems = apiResult["data"]["failedItems"]
            }
        }

        // cmdbConfig检查
        if (this.cmdbConfig != null && this.cmdbConfig.size() > 0) {
            def configs = []
            for (def item in this.cmdbConfig) {
                checkEmpty("cmdbConfig.key", item["key"])
                checkEmpty("cmdbConfig.validateType，key=" + item["key"], item.validateType)

                if (ValidateUtils.ignore(this.clusterName, this.virtualEnv, item)) {
                    this.context.jenkins.echo "cmdbConfig-忽略配置检查：item=${item["key"]}, cluster=${this.clusterName}, virtualEnv=${this.virtualEnv}"
                } else {
                    configs.add(item)
                }
            }

            if (configs.size() > 0) {
                def apiResult = new LateinosFrameworkApiCmd(this.context)
                        .setClusterName(this.clusterName)
                        .setVirtualEnv(this.virtualEnv)
                        .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                        .setApiMethod("POST")
                        .setApiPath("/v1/configuration/validate/cmdb")
                        .setApiBody([validateOptions: configs, vfs: this.virtualEnv, projectName: this.projectName])
                        .execute().getResult()
                this.context.jenkins.echo "apiResult = ${apiResult}"

                isAllSuccessed = apiResult["data"]["allSuccessed"] != null ? apiResult["data"]["allSuccessed"] : false
                configValidateResult.cmdbFailedItems = apiResult["data"]["failedItems"]
            }
        }

        // 都没填写直接跳过
        if (configValidateResult.globalFailedItems == null
                && configValidateResult.virtualFailedItems == null
                && configValidateResult.cmdbFailedItems == null) {
            isAllSuccessed = true
        }

        configValidateResult.allSuccessed = isAllSuccessed
        configValidateResult.clusterName = this.clusterName
        configValidateResult.virtualEnv = this.virtualEnv
        configValidateResult.projectName = this.projectName
        configValidateResult.globalConfig = this.globalConfig
        configValidateResult.virtualEnvConfig = this.virtualEnvConfig
        configValidateResult.cmdbConfig = this.cmdbConfig

        this.resultImpl = configValidateResult
        return this
    }

    @Override
    ConfigValidateResult getResult() {
        return this.resultImpl
    }

    String getClusterName() {
        return clusterName
    }

    ConfigValidateCmd setClusterName(String clusterName) {
        this.clusterName = clusterName
        return this
    }

    String getProjectName() {
        return projectName
    }

    ConfigValidateCmd setProjectName(String projectName) {
        this.projectName = projectName
        return this
    }

    String getVirtualEnv() {
        return virtualEnv
    }

    ConfigValidateCmd setVirtualEnv(String virtualEnv) {
        this.virtualEnv = virtualEnv
        return this
    }

    def getGlobalConfig() {
        return globalConfig
    }

    ConfigValidateCmd setGlobalConfig(def globalConfig) {
        this.globalConfig = globalConfig
        return this
    }

    def getVirtualEnvConfig() {
        return virtualEnvConfig
    }

    ConfigValidateCmd setVirtualEnvConfig(def virtualEnvConfig) {
        this.virtualEnvConfig = virtualEnvConfig
        return this
    }

    def getCmdbConfig() {
        return cmdbConfig
    }

    ConfigValidateCmd setCmdbConfig(def cmdbConfig) {
        this.cmdbConfig = cmdbConfig
        return this
    }
}
