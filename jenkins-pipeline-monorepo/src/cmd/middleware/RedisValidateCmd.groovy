package cmd.middleware


import cmd.LateinosFrameworkApiCmd
import cmd.utils.FrameworkApiUtils
import cmd.utils.ValidateUtils
import entity.middleware.BaseValidateResult

/**
 * 检查redis配置
 */
class RedisValidateCmd extends AbstractMiddlewareValidateCmd {

    RedisValidateCmd(context) {
        super(context)
    }

    @Override
    AbstractMiddlewareValidateCmd execute() {
        checkEmpty("clusterName", this.clusterName)
        checkEmpty("config", this.config)

        boolean isAllSuccessed = false
        def failedItems = [:]

        for (def item in this.config) {
            checkEmpty("cacheCode", item["cacheCode"])

            if (ValidateUtils.ignore(this.clusterName, this.virtualEnv, item)) {
                this.context.jenkins.echo "中间件-Redis-忽略配置检查：item=${item.cacheCode}, cluster=${this.clusterName}, virtualEnv=${this.virtualEnv}"
                continue
            }

            // 接口调用
            def apiResult = new LateinosFrameworkApiCmd(this.context)
                    .setClusterName(this.clusterName)
                    .setVirtualEnv(this.virtualEnv)
                    .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                    .setApiMethod("POST")
                    .setApiPath("/v1/redis/config/list")
                    .setApiBody([cacheCode: item.cacheCode])
                    .execute().getResult()
            this.context.jenkins.echo "apiResult = ${apiResult}"

            // 结果解析
            if (apiResult == null || apiResult["data"] == null || apiResult["data"]["totalCount"] <= 0) {
                failedItems[item.cacheCode] = "Redis配置不存在"
            }
        }

        if (failedItems.size() <= 0) {
            isAllSuccessed = true
        }

        this.resultImpl = new BaseValidateResult()
        this.resultImpl.allSuccessed = isAllSuccessed
        this.resultImpl.failedItems = failedItems
        this.resultImpl.clusterName = this.clusterName
        this.resultImpl.virtualEnv = this.virtualEnv

        return this
    }
}
