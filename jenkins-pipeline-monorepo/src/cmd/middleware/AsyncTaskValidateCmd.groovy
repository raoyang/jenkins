package cmd.middleware


import cmd.LateinosFrameworkApiCmd
import cmd.utils.FrameworkApiUtils
import cmd.utils.ValidateUtils
import entity.middleware.BaseValidateResult

/**
 * 检查异步任务调度配置
 */
class AsyncTaskValidateCmd extends AbstractMiddlewareValidateCmd {

    AsyncTaskValidateCmd(context) {
        super(context)
    }

    @Override
    AbstractMiddlewareValidateCmd execute() {
        checkEmpty("clusterName", this.clusterName)
        checkEmpty("config", this.config)

        boolean isAllSuccessed = false
        def failedItems = [:]

        for (def item in this.config) {
            checkEmpty("taskTypeId", item["taskTypeId"])
            checkEmpty("scheduleKey", item["scheduleKey"])

            if (ValidateUtils.ignore(this.clusterName, this.virtualEnv, item)) {
                this.context.jenkins.echo "中间件-异步任务调度-忽略配置检查：item=${item.taskTypeId}, cluster=${this.clusterName}, virtualEnv=${this.virtualEnv}"
                continue
            }

            // 检查任务类型配置
            def apiResult = new LateinosFrameworkApiCmd(this.context)
                    .setClusterName(this.clusterName)
                    .setVirtualEnv(this.virtualEnv)
                    .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RT_ASYNCTASK_DAO)
                    .setApiMethod("GET")
                    .setApiPath("/v1/tasktype/${item.taskTypeId}")
                    .execute().getResult()
            this.context.jenkins.echo "apiResult = ${apiResult}"

            if (apiResult == null || apiResult["data"] == null) {
                failedItems[item.taskTypeId] = "异步任务调度-任务类型配置不存在"
            }

            // 检查调度策略配置
            apiResult = new LateinosFrameworkApiCmd(this.context)
                    .setClusterName(this.clusterName)
                    .setVirtualEnv(this.virtualEnv)
                    .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RT_ASYNCTASK_DAO)
                    .setApiMethod("GET")
                    .setApiPath("/v1/schedulestrategy/${item.scheduleKey}")
                    .execute().getResult()
            this.context.jenkins.echo "apiResult = ${apiResult}"

            if (apiResult == null || apiResult["data"] == null) {
                failedItems[item.scheduleKey] = "异步任务调度-调度策略配置不存在"
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
