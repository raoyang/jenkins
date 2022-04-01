package pipeline.ugengine.upgrade

import cmd.AbstractCmd
import cmd.LateinosFrameworkApiCmd
import entity.Context

class GetShardImageCMD extends AbstractCmd<Object> {
    GetShardImageCMD(Context context){
        super(context)
        this.apiPath = "/v1/engine/details/{shard}"
        this.apiMethod ="GET"
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

    String getShardKey() {
        return shardKey
    }

    AbstractCmd setShardKey(String shardKey) {
        this.shardKey = shardKey
        return this
    }
    String shardKey

    private String apiPath
    private String apiMethod
    private String remoteServiceName

    private def executedResult

    private String constructApiPath() {
        return this.apiPath.replace("{shard}", this.shardKey)
    }

    @Override
    AbstractCmd execute() {
        /* 输入参数校验*/
        checkEmpty("clusterName", this.clusterName)
        checkEmpty("remoteServiceName", this.remoteServiceName)
        checkEmpty("apiMethod", this.apiMethod)
        checkEmpty("apiPath", this.apiPath)

        String newPath = constructApiPath()
        this.context.jenkins.echo "shard upgrade reqBody = ${newPath}"
        LateinosFrameworkApiCmd apiCmd = new LateinosFrameworkApiCmd(context)
        this.executedResult = apiCmd.setClusterName(clusterName)
                .setRemoteServiceName(remoteServiceName)
                .setApiMethod(this.apiMethod)
                .setApiPath(newPath)
                .execute()
                .getResult()
        this.context.jenkins.echo "shard details result = ${this.executedResult}"
        return this
    }

    @Override
    Object getResult() {
        return  this.executedResult
    }
}
