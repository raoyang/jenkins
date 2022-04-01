package pipeline.ugengine

import cmd.BuildNotifyCmd
import cmd.InputMessageCmd
import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess
import pipeline.ugengine.autotest.AutoTestExecuteCMD
import pipeline.ugengine.autotest.UploadReportHtmlCMD
import pipeline.ugengine.upgrade.ShardUpgradeCMD

/**
 *  cleaner开发流程流水线
 */
class DevCoreProcess extends BaseProcess {
    DevCoreProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [UgEngineProjectNames.ENGINE_CORE_FILE_OPERATOR,
                UgEngineProjectNames.ENGINE_CORE_COLUMNCHANGE_EXECUTOR,
                UgEngineProjectNames.ENGINE_CORE_METADATA_DAO,
                UgEngineProjectNames.ENGINE_CORE_ENGINE_DAO]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)
        ProjectItemConfig config_engine_dao = context.projectsConfig.get(UgEngineProjectNames.ENGINE_CORE_ENGINE_DAO)
        ProjectItemConfig config_file_operator = context.projectsConfig.get(UgEngineProjectNames.ENGINE_CORE_FILE_OPERATOR)
        ProjectItemConfig config_columnchange_executor = context.projectsConfig.get(UgEngineProjectNames.ENGINE_CORE_COLUMNCHANGE_EXECUTOR)
        ProjectItemConfig config_metadata_dao = context.projectsConfig.get(UgEngineProjectNames.ENGINE_CORE_METADATA_DAO)

        this.finishedNotifyCmd.addToNotifyUserList(config_file_operator.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(config_engine_dao, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_file_operator, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_columnchange_executor, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_metadata_dao, DeployCluster.UG_DEV)
        }

        this.context.jenkins.stage("代码扫描") {
            sonarCheck(config_engine_dao)
            sonarCheck(config_file_operator)
            sonarCheck(config_columnchange_executor)
            sonarCheck(config_metadata_dao)
        }

        String image_engine_dao = buildAppProjectImage(config_engine_dao)
        String image_file_operator = buildAppProjectImage(config_file_operator)
        String image_columnchange_executor = buildAppProjectImage(config_columnchange_executor)
        String image_metadata_dao = buildAppProjectImage(config_metadata_dao)
        String originImage_metadata_dao = this.getDeployImage(config_metadata_dao, "dev")

        this.context.jenkins.echo "h3yun-engine-dao current image url : ${image_engine_dao}"
        //h3yun-engine-dao没有更新不需要执行部署
        if(image_engine_dao.length() > 0) {
            deployAppProject_Engine_Shard_BySerial(config_engine_dao, image_engine_dao)
        }

        deployAppProject(config_file_operator, image_file_operator)
        deployAppProject(config_columnchange_executor, image_columnchange_executor)
        deployAppProject(config_metadata_dao, image_metadata_dao)

        //执行测试
        String executeTestResult
        ProjectItemConfig config_autotest_job = context.projectsConfig.get(UgEngineProjectNames.ENGINE_AUTOTEST)
        this.context.jenkins.stage("执行场景测试") {
            AutoTestExecuteCMD test_executor = new AutoTestExecuteCMD(context)
            executeTestResult = test_executor.setCluster(DeployCluster.UG_DEV)
                    .setConfig_autotest(config_autotest_job)
                    .setProjectName(config_autotest_job.projectName)
                    .setRunEnv("dev")
                    .execute()
                    .getResult()
            this.context.jenkins.echo "自动化测试结果：${executeTestResult}"
        }
        String reportHtml = uploadReportHtml(config_autotest_job)
        finishedNotifyCmd.buttonUrl["自动化测试报告"] = reportHtml
        if(executeTestResult == "1") {
            finishedNotifyCmd.setErrorNotify(true)
            finishedNotifyCmd.notifyMsg["失败原因"] = "<font color=#FF3300 size=5 face=\"黑体\">自动化测试执行不通过，请点击下方的<a target=\"_blank\" href=\"${reportHtml}\">【自动化测试报告】</a>链接查看详情</font>"
            this.context.jenkins.echo "自动化测试失败，开始回滚..."
            if(originImage_metadata_dao != image_metadata_dao) {
                rollbackProject(config_metadata_dao, originImage_metadata_dao)
            }

//            for(UpgradeShardResult upgradeResult in UpgradeShardResultList) {
//                if(upgradeResult != null && upgradeResult.getRelease_result() == "success") {
//                    //回滚shard到旧的镜像
//                    deployAppProject_Engine_Shard(upgradeResult.getShard_key(),
//                            config_engine_dao,
//                            upgradeResult.getOrigin_image_url())
//                }
//            }
        }
    }

    private String buildAppProjectImage(ProjectItemConfig config) {
        String image_url = ""
        if(!config.needBuild) {
            return image_url
        }
        this.context.jenkins.stage("编译镜像${config.projectName}") {
            image_url = this.buildAppProject(config)
        }
        return image_url
    }

    private void deployAppProject(ProjectItemConfig config, String image_url){
        if(!config.needBuild) {
            return
        }
        this.context.jenkins.stage("部署${config.projectName}") {
            this.deployAppProjectYaml(config
                    , image_url
                    , "dev"
                    , "h3yun-engine")
        }
    }

    private def deployAppProject_Engine_Shard(String shardKey, ProjectItemConfig config, String image_url){
        if(image_url.length() == 0){
            return null
        }
        this.context.jenkins.stage("部署${config.projectName}-${shardKey}"){
            //执行升级
            ShardUpgradeCMD upgradeCMD = new ShardUpgradeCMD(context)
            //获取镜像版本
            String version = upgradeCMD.get_image_version(image_url)
            def upgradeResult = upgradeCMD.setImage(image_url)
                    .setShardKey(shardKey)
                    .setVersion(version)
                    .setDescription("流水线更新")
                    .setClusterName(DeployCluster.UG_DEV.getCode())
                    .execute().getResult()
            this.context.jenkins.echo "update ${config.projectName}-${shardKey} result : ${upgradeResult}"
            return upgradeResult
        }
    }

    private def deployAppProject_Engine_Shard_ByInput(ProjectItemConfig config, String image_url) {
        if(image_url.length() == 0){
            return null
        }
        ArrayList<String> toDeployShards = []
        Map shardDeployResult = [:]
        boolean allShardIsDeployed = false
        String[] shards = ["shard1","shard2"]
        for(String shard : shards) {
            shardDeployResult[shard] = false
        }
        while (!allShardIsDeployed) {
            this.context.jenkins.stage("选择需要部署的shard"){
                //通知
                sendShardChoiceNotify(config)
                InputMessageCmd messageCmd = new InputMessageCmd(this.context, this)
                        .setOkBtnText("确认")
                        .setNotifyMsg("启动H3yun-engine-dao服务部署，请确认需要部署的shard")

                for (String shard : shards) {
                    messageCmd.addParameter(this.context.jenkins.booleanParam(
                            name: "deploy-${shard}",
                            description: "部署h3yun-engine-dao-${shard}",
                            defaultValue: false
                    ))
                }

                def inputResult = messageCmd.execute().getResult()
                toDeployShards.clear()
                for (String shard : shards) {
                    if (inputResult["deploy-${shard}"]) {
                        toDeployShards.add(shard)
                    }
                }
            }
            if(toDeployShards.size() == 0){
                allShardIsDeployed = true
            }
            for(String shard : toDeployShards) {
                deployAppProject_Engine_Shard(shard, config, image_url)
                shardDeployResult[shard] = true
            }
            boolean tmpAllDeployed = true
            for(String shard : shards) {
                if(!shardDeployResult[shard]) {
                    tmpAllDeployed = false
                }
            }
            allShardIsDeployed = tmpAllDeployed
        }
    }

    private def deployAppProject_Engine_Shard_BySerial(ProjectItemConfig config, String image_url) {
        if(image_url.length() == 0){
            return null
        }
        String[] shrads = ["shard1","shard2"]
        for (String shard : shrads) {
            deployAppProject_Engine_Shard(shard, config, image_url)
        }
    }

    /**
     * 发送提醒选择shard的通知
     * @return
     */
    BuildNotifyCmd sendShardChoiceNotify(ProjectItemConfig config) {
        BuildNotifyCmd notifyCmd = this.constructNotify()
        notifyCmd.addToNotifyUserList(config.projectMaintainer)
        notifyCmd.setTodoNotify(true)
        notifyCmd.notifyMsg["信息"] = "请选择需要更新的shard"
        notifyCmd.buttonUrl["输入链接"] = "${this.context.jenkins.env.BUILD_URL}/input"
        notifyCmd.execute()
    }

    private void rollbackProject(ProjectItemConfig itemConfig, String originImage) {
        this.context.jenkins.stage("执行回滚${itemConfig.projectName}") {
            this.rollbackDeployImage(itemConfig
                    , originImage
                    , "dev")
            this.checkAppProjectRunningSuccess(itemConfig
                    , originImage
                    , DeployCluster.UG_DEV
                    , "h3yun-engine"
                    , true)
        }
    }

    private String uploadReportHtml(ProjectItemConfig config_autotest){
        UploadReportHtmlCMD uploadReportHtmlCMD = new UploadReportHtmlCMD(this.context)
        return uploadReportHtmlCMD.setProjectName(config_autotest.projectName)
                            .execute().getResult()
    }
}
