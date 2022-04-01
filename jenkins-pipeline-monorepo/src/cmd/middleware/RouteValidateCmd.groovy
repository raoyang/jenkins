package cmd.middleware


import cmd.LateinosFrameworkApiCmd
import cmd.utils.FrameworkApiUtils
import cmd.utils.ValidateUtils
import entity.middleware.BaseValidateResult

/**
 * 检查路由配置
 */
class RouteValidateCmd extends AbstractMiddlewareValidateCmd {

    RouteValidateCmd(context) {
        super(context)
    }

    @Override
    AbstractMiddlewareValidateCmd execute() {
        checkEmpty("clusterName", this.clusterName)
        checkEmpty("config", this.config)

        boolean isAllSuccessed = false
        def failedItems = [:]

        def validateOptions = []
        def validateResult = null

        // 查询全局路由
        if (virtualEnv == null || virtualEnv.size() <= 0) {
            for (def item in this.config) {
                checkEmpty("serviceName", item["serviceName"])
                String key = item["serviceName"]

                if (ValidateUtils.ignore(this.clusterName, null, item)) {
                    this.context.jenkins.echo "中间件-路由-忽略配置检查：item=${key}, cluster=${this.clusterName}"
                } else {
                    def configItem = [key: "/lateinos/route/${key}", validateType: "NOT_BLANK"]
                    validateOptions.add(configItem)
                }
            }

            def apiResult = new LateinosFrameworkApiCmd(this.context)
                    .setClusterName(this.clusterName)
                    .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                    .setApiMethod("POST")
                    .setApiPath("/v1/configuration/validate/global")
                    .setApiBody([validateOptions: validateOptions])
                    .execute().getResult()
            this.context.jenkins.echo "apiResult = ${apiResult}"

            isAllSuccessed = apiResult["data"]["allSuccessed"] != null ? apiResult["data"]["allSuccessed"] : false
            validateResult = apiResult["data"]["failedItems"]
        }
        // 查询虚拟环境路由
        else {
            for (def item in config) {
                checkEmpty("serviceName", item["serviceName"])
                String key = item["serviceName"]

                if (ValidateUtils.ignore(this.clusterName, this.virtualEnv, item)) {
                    this.context.jenkins.echo "中间件-路由-忽略配置检查：item=${key}, cluster=${this.clusterName}, virtualEnv=${this.virtualEnv}"
                } else {
                    def configItem = [key: "/lateinos/route/${key}", validateType: "NOT_BLANK"]
                    validateOptions.add(configItem)
                }
            }

            def apiResult = new LateinosFrameworkApiCmd(this.context)
                    .setClusterName(this.clusterName)
                    .setVirtualEnv(this.virtualEnv)
                    .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                    .setApiMethod("POST")
                    .setApiPath("/v1/configuration/validate/vfs")
                    .setApiBody([validateOptions: validateOptions, vfs: this.virtualEnv])
                    .execute().getResult()
            this.context.jenkins.echo "apiResult = ${apiResult}"

            isAllSuccessed = apiResult["data"]["allSuccessed"] != null ? apiResult["data"]["allSuccessed"] : false
            validateResult = apiResult["data"]["failedItems"]
        }

        if (validateResult != null) {
            for (def failedItem in validateResult) {
                failedItems[failedItem] = "路由信息不存在"
            }
        }

        this.resultImpl = new BaseValidateResult()
        this.resultImpl.allSuccessed = isAllSuccessed
        this.resultImpl.failedItems = failedItems
        this.resultImpl.clusterName = this.clusterName
        this.resultImpl.virtualEnv = this.virtualEnv

        return this
    }
}
