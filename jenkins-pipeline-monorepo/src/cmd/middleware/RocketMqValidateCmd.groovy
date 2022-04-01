package cmd.middleware


import cmd.LateinosFrameworkApiCmd
import cmd.utils.FrameworkApiUtils
import cmd.utils.ValidateUtils
import entity.middleware.BaseValidateResult

/**
 * 检查rocketMq配置
 */
class RocketMqValidateCmd extends AbstractMiddlewareValidateCmd {

    RocketMqValidateCmd(context) {
        super(context)
    }

    @Override
    AbstractMiddlewareValidateCmd execute() {
        checkEmpty("clusterName", this.clusterName)
        checkEmpty("config", this.config)

        boolean isAllSuccessed = false
        def failedItems = [:]

        for (def item in this.config) {
            checkEmpty("type", item["type"])
            checkEmpty("topic", item["topic"])
            checkEmpty("code", item["code"])

            if (ValidateUtils.ignore(this.clusterName, this.virtualEnv, item)) {
                this.context.jenkins.echo "中间件-RocketMQ-忽略配置检查：item=${item.topic}, cluster=${this.clusterName}, virtualEnv=${this.virtualEnv}"
                continue
            }

            // 检查topic配置
            def apiResult = new LateinosFrameworkApiCmd(this.context)
                    .setClusterName(this.clusterName)
                    .setVirtualEnv(this.virtualEnv)
                    .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                    .setApiMethod("POST")
                    .setApiPath("/v1/rocketmq/topic/list")
                    .setApiBody([topic: item.topic])
                    .execute().getResult()
            this.context.jenkins.echo "apiResult = ${apiResult}"

            if (apiResult == null || apiResult["data"] == null || apiResult["data"]["totalCount"] <= 0) {
                failedItems[item.topic] = "RocketMq-topic配置不存在"
            }

            switch (item.type) {
                case "producer":
                    // 检查消息提供者配置
                    apiResult = new LateinosFrameworkApiCmd(this.context)
                            .setClusterName(this.clusterName)
                            .setVirtualEnv(this.virtualEnv)
                            .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                            .setApiMethod("POST")
                            .setApiPath("/v1/rocketmq/producer/list")
                            .setApiBody([code: item.code])
                            .execute().getResult()
                    this.context.jenkins.echo "apiResult = ${apiResult}"

                    if (apiResult == null || apiResult["data"] == null || apiResult["data"]["totalCount"] <= 0) {
                        failedItems[item.code] = "RocketMq-producer配置不存在"
                    }

                    break
                case "consumer":
                    // 检查消息消费者配置
                    apiResult = new LateinosFrameworkApiCmd(this.context)
                            .setClusterName(this.clusterName)
                            .setVirtualEnv(this.virtualEnv)
                            .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                            .setApiMethod("POST")
                            .setApiPath("/v1/rocketmq/consumer/list")
                            .setApiBody([code: item.code])
                            .execute().getResult()
                    this.context.jenkins.echo "apiResult = ${apiResult}"

                    if (apiResult == null || apiResult["data"] == null || apiResult["data"]["totalCount"] <= 0) {
                        failedItems[item.code] = "RocketMq-consumer配置不存在"
                    }

                    break
                default:
                    throw new IllegalArgumentException("RocketMq配置检查异常: 不支持的配置类型${item.type}")
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
