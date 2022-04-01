package cmd.middleware


import cmd.LateinosFrameworkApiCmd
import cmd.utils.FrameworkApiUtils
import cmd.utils.ValidateUtils
import entity.middleware.BaseValidateResult

/**
 * 检查日志采集配置
 */
class LogcollectorValidateCmd extends AbstractMiddlewareValidateCmd {

    LogcollectorValidateCmd(context) {
        super(context)
    }

    @Override
    AbstractMiddlewareValidateCmd execute() {
        checkEmpty("clusterName", this.clusterName)
        checkEmpty("config", this.config)

        boolean isAllSuccessed = false
        def failedItems = [:]

        for (def item in this.config) {
            // eventKey检查
            String eventKey = item["eventKey"]
            checkEmpty("eventKey", eventKey)

            if (ValidateUtils.ignore(this.clusterName, this.virtualEnv, item)) {
                this.context.jenkins.echo "中间件-日志采集-忽略配置检查：item=${eventKey}, cluster=${this.clusterName}, virtualEnv=${this.virtualEnv}"
                continue
            }

            def apiResult = new LateinosFrameworkApiCmd(this.context)
                    .setClusterName(this.clusterName)
                    .setVirtualEnv(this.virtualEnv)
                    .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                    .setApiMethod("GET")
                    .setApiPath("/v1/logcollector/config?pageIndex=0&pageSize=10&eventKey=${eventKey}")
                    .execute().getResult()
            if (apiResult == null || apiResult["data"] == null || apiResult["data"]["totalCount"] <= 0) {
                failedItems[eventKey] = "日志采集-eventKey配置不存在"
            } else {
                this.context.jenkins.echo "apiResult = ${apiResult}"

                // collector检查
                checkEmpty("collector", item["collector"])
                checkEmpty("collector.type", item["collector"]["type"])
                checkEmpty("collector.code", item["collector"]["code"])

                apiResult = new LateinosFrameworkApiCmd(this.context)
                        .setClusterName(this.clusterName)
                        .setVirtualEnv(this.virtualEnv)
                        .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                        .setApiMethod("GET")
                        .setApiPath("/v1/logcollector/beat/version/page?pageIndex=0&pageSize=10&beatType=${item.collector.type}&code=${item.collector.code}")
                        .execute().getResult()
                if (apiResult == null || apiResult["data"] == null || apiResult["data"]["totalCount"] <= 0) {
                    failedItems["${item.collector.type}:${item.collector.code}"] = "日志采集-采集器配置不存在"
                } else {
                    this.context.jenkins.echo "apiResult = ${apiResult["data"]["totalCount"]}"
                    Long beatVersionId = null
                    for (def beatVersion in apiResult["data"]["resultList"]) {
                        if (beatVersion["status"] == 1) {
                            beatVersionId = beatVersion["id"]
                            break
                        }
                    }
                    if (beatVersionId == null) {
                        failedItems["${item.collector.type}:${item.collector.code}"] = "日志采集-没有开启的采集器配置"
                    } else {
                        apiResult = new LateinosFrameworkApiCmd(this.context)
                                .setClusterName(this.clusterName)
                                .setVirtualEnv(this.virtualEnv)
                                .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                                .setApiMethod("GET")
                                .setApiPath("/v1/logcollector/beat/detail/${beatVersionId}")
                                .execute().getResult()
                        if (apiResult == null || apiResult["data"] == null || apiResult["data"]["items"] == null || apiResult["data"]["items"].size() <= 0) {
                            failedItems["${item.collector.type}:${item.collector.code}"] = "日志采集-采集器配置详情不存在"
                            break
                        }
                        boolean isBeatItemExist = false
                        for (def beatItem in apiResult["data"]["items"]) {
                            if (eventKey.equals(beatItem["eventKey"])) {
                                this.context.jenkins.echo "apiResult = ${beatItem["itemType"]}: ${beatItem["content"]}"
                                isBeatItemExist = true
                                break
                            }
                        }
                        if (!isBeatItemExist) {
                            failedItems["${item.collector.type}:${item.collector.code}"] = "日志采集-采集器配置项不存在"
                        }
                    }
                }
            }

            // channel检查
            if (item["channel"] != null) {
                checkEmpty("channel.type", item["channel"]["type"])
                checkEmpty("channel.code", item["channel"]["code"])

                apiResult = new LateinosFrameworkApiCmd(this.context)
                        .setClusterName(this.clusterName)
                        .setVirtualEnv(this.virtualEnv)
                        .setRemoteServiceName(FrameworkApiUtils.LATEINOS_RUNTIME_SERVER)
                        .setApiMethod("GET")
                        .setApiPath("/v1/logcollector/channel/version/page?pageIndex=0&pageSize=10&channelType=${item.channel.type}&code=${item.channel.code}&eventKey=${eventKey}&status=1")
                        .execute().getResult()
                this.context.jenkins.echo "apiResult = ${apiResult}"

                if (apiResult == null || apiResult["data"] == null || apiResult["data"]["totalCount"] <= 0) {
                    failedItems["${item.channel.type}:${item.channel.code}"] = "日志采集-管道配置不存在"
                }
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
