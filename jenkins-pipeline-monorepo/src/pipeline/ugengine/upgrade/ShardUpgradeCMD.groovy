package pipeline.ugengine.upgrade

import cmd.AbstractCmd
import cmd.LateinosFrameworkApiCmd
import entity.Context

class ShardUpgradeCMD extends AbstractCmd<Object> {

    ShardUpgradeCMD(Context context) {
        super(context)
        this.apiPath = '/v1/engine/update'
        this.apiMethod = 'PUT'
        this.remoteServiceName = 'h3yun-engine-controller'
    }
    /*h3yun-engine-controller update接口需要参数*/
    String getImage() {
        return image
    }

    ShardUpgradeCMD setImage(String image) {
        this.image = image
        return this
    }
    String image;

    String getShardKey() {
        return shardKey
    }

    ShardUpgradeCMD setShardKey(String shradKey) {
        this.shardKey = shradKey
        return this
    }
    String shardKey;

    String getVersion() {
        return version
    }

    ShardUpgradeCMD setVersion(String version) {
        this.version = version
        return this
    }
    String version;

    String getDescription() {
        return description
    }

    ShardUpgradeCMD setDescription(String description) {
        this.description = description
        return this
    }
    String description;

    /*集群调用需要的参数*/
    String getClusterName() {
        return clusterName
    }

    ShardUpgradeCMD setClusterName(String clusterName) {
        this.clusterName = clusterName
        return this
    }
    String clusterName;

    String getRemoteServiceName() {
        return remoteServiceName
    }

    ShardUpgradeCMD setRemoteServiceName(String remoteServiceName) {
        this.remoteServiceName = remoteServiceName
        return this
    }
    String remoteServiceName;
    String apiMethod;
    String apiPath;

    private UpgradeShardResult executedResult

    private  Map constructBody() {
        checkEmpty("image", this.image)
        checkEmpty("shard", this.shardKey)
        Map req = [
                "description": this.description,
                "image": this.image,
                "shard": this.shardKey,
                "version": this.version
        ]
         return ["engines" : [req]]
    }

    @Override
    AbstractCmd execute() {
        /* 输入参数校验*/
        checkEmpty("clusterName", this.clusterName)
        checkEmpty("remoteServiceName", this.remoteServiceName)
        checkEmpty("apiMethod", this.apiMethod)
        checkEmpty("apiPath", this.apiPath)
        checkEmpty("version", this.version)
        checkEmpty("shardKey", this.shardKey)

        this.executedResult = new UpgradeShardResult()
        this.executedResult.setShard_key(this.shardKey)

        //保存更新前的版本
        this.executedResult.setOrigin_image_url(getShardCurrentImage(this.shardKey, this.clusterName))

        //调用升级接口
        Map reqBody = constructBody()
        this.context.jenkins.echo "shard upgrade reqBody = ${reqBody}"
        LateinosFrameworkApiCmd apiCmd = new LateinosFrameworkApiCmd(context)
        def tempUpgradeResult = apiCmd.setClusterName(clusterName)
            .setRemoteServiceName(remoteServiceName)
            .setApiMethod(this.apiMethod)
            .setApiPath(this.apiPath)
            .setApiBody(reqBody)
            .execute()
            .getResult()
        this.context.jenkins.echo "tempUpgradeResult = ${tempUpgradeResult}"
        if(tempUpgradeResult.errorCode != null){
            throw new Exception("errorCode:${tempUpgradeResult.errorCode},errorMsg:${tempUpgradeResult.errorMessage}")
        }
        String updateShardKey = tempUpgradeResult.data.key

        //确认是否升级成功
        boolean deployCompeleted = false
        while(!deployCompeleted) {
            def releaseResult = getShardDeployResult(updateShardKey, this.clusterName)
            String resultString = releaseResult.data[0].result
            this.executedResult.setRelease_result(resultString)
            if(resultString == "failure") {
                this.context.jenkins.echo " release result : ${releaseResult}"
                throw new Exception("h3yun-engine-dao-${shardKey}部署失败")
                //失败时需要人工介入修改配置
            }
            if(resultString == "success") {
                this.context.jenkins.echo " release result : ${releaseResult}"
                deployCompeleted = true
            }
        }
        return this
    }

    @Override
    Object getResult() {
        return this.executedResult
    }

    //获取更新前的镜像版本
    private String getShardCurrentImage(String shardKey, String clusterName) {
        GetShardImageCMD getShardImageCMD = new GetShardImageCMD(this.context)
        def result = getShardImageCMD.setClusterName(clusterName)
                .setShardKey(shardKey)
                .execute()
                .getResult()
        this.context.jenkins.echo "current shard ${shardKey} images details : ${result}"
        return result.data.images
    }

    //查询发布结果
    private Map getShardDeployResult(String shardKey, String clusterName){
        ShardUpgradeResultCMD shardUpgradeResultCMD = new ShardUpgradeResultCMD(this.context)
        return shardUpgradeResultCMD.setClusterName(clusterName)
                .setUpdateShardResultKey(shardKey)
                .execute().getResult()
    }

    //获取镜像版本,随着最后一位镜像版本号的递增，年会显示不全
    String get_image_version(String image_url) {
        Integer image_url_length = image_url.length()
        String version = image_url.substring(image_url.length()-16, image_url_length)
        if(version.trim().startsWith(".")) {
            return version.trim().substring(1)
        }
        return version
    }
}
