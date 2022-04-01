package pipeline.ugengine.upgrade

import cmd.AbstractCmd
import cmd.LateinosFrameworkApiCmd
import entity.Context

/*获取shard更新的结果*/
class ShardUpgradeResultCMD extends AbstractCmd<Object> {

    ShardUpgradeResultCMD(Context context){
        super(context)
        this.apiPath = "/v1/engine/release/result"
        this.apiMethod ="POST"
        this.remoteServiceName = 'h3yun-engine-controller'
    }

    String getClusterName() {
        return clusterName
    }

    AbstractCmd setClusterName(String clusterName) {
        this.clusterName = clusterName
        return this
    }
    String clusterName

    String getUpdateShardResultKey() {
        return updateShardResultKey
    }

    AbstractCmd setUpdateShardResultKey(String updateShardResultKey) {
        this.updateShardResultKey = updateShardResultKey
        return this
    }
    String updateShardResultKey

    private String apiPath
    private String apiMethod
    private String remoteServiceName

    private def executedResult

    @Override
    AbstractCmd execute() {
        /* 输入参数校验*/
        checkEmpty("clusterName", this.clusterName)
        checkEmpty("remoteServiceName", this.remoteServiceName)
        checkEmpty("apiMethod", this.apiMethod)
        checkEmpty("apiPath", this.apiPath)

        def reqBody = constructBody()
        //this.context.jenkins.echo "shard upgrade reqBody = ${reqBody}"
        LateinosFrameworkApiCmd apiCmd = new LateinosFrameworkApiCmd(context)
        this.executedResult = apiCmd.setClusterName(clusterName)
                .setRemoteServiceName(remoteServiceName)
                .setApiMethod(this.apiMethod)
                .setApiPath(this.apiPath)
                .setApiBody(reqBody)
                .execute()
                .getResult()
        //this.context.jenkins.echo "shard release result = ${this.executedResult}"
        return this
    }

    private def constructBody() {
        checkEmpty("updateShardResultKey", this.updateShardResultKey)
        def req = [this.updateShardResultKey]
        return req
    }


    @Override
    Object getResult() {
        this.executedResult
    }
}
