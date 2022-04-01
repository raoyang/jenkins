package pipeline.athena

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

/**
 *
 */
abstract class UgEnvPublishProcess extends BaseProcess {
    UgEnvPublishProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [AthenaProjectNames.MANAGE_WEBAPP
                , AthenaProjectNames.WEBADMIN_GATEWAY
                , AthenaProjectNames.WEBADMIN_SERVER]
    }

    abstract protected DeployCluster getDeployCluster()
    abstract protected String getArgoDeployEnv()
    abstract protected String getK8sNamespace()
    abstract protected String getTargetBranchNameAfterVerify()

    @Override
    protected void execute() {
        ProjectItemConfig webadminGatewayConfig = this.context.projectsConfig.get(AthenaProjectNames.WEBADMIN_GATEWAY)
        String originWebadminGatewayImage = ""
        String targetWebadminGatewayImage = ""

        ProjectItemConfig webadminServerConfig = this.context.projectsConfig.get(AthenaProjectNames.WEBADMIN_SERVER)
        String originWebadminServerImage = ""
        String targetWebadminServerImage = ""

        ProjectItemConfig manageWebappConfig = this.context.projectsConfig.get(AthenaProjectNames.MANAGE_WEBAPP)
        String originManageWebappImage = ""
        String targetManageWebappImage = ""

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(webadminGatewayConfig, getDeployCluster())
            this.projectConfigCheck(webadminServerConfig, getDeployCluster())
            this.projectConfigCheck(manageWebappConfig, getDeployCluster())
        }

        this.context.jenkins.stage("编译镜像集") {
            targetWebadminGatewayImage = this.buildAppProject(webadminGatewayConfig)
            targetWebadminServerImage = this.buildAppProject(webadminServerConfig)
            targetManageWebappImage = this.buildAppProject(manageWebappConfig)
        }

        this.context.jenkins.stage("保存部署信息") {
            originWebadminGatewayImage = this.getDeployImage(webadminGatewayConfig, getArgoDeployEnv())
            originWebadminServerImage = this.getDeployImage(webadminServerConfig, getArgoDeployEnv())
            originManageWebappImage = this.getDeployImage(manageWebappConfig, getArgoDeployEnv())
        }

        this.context.jenkins.stage("提交部署YAML集") {
            this.deployAppProjectYaml(webadminGatewayConfig
                    , targetWebadminGatewayImage
                    , getArgoDeployEnv()
                    , getK8sNamespace())
            this.deployAppProjectYaml(webadminServerConfig
                    , targetWebadminServerImage
                    , getArgoDeployEnv()
                    , getK8sNamespace())
            this.deployAppProjectYaml(manageWebappConfig
                    , targetManageWebappImage
                    , getArgoDeployEnv()
                    , getK8sNamespace())
        }

        this.context.jenkins.stage("检测部署") {
            this.checkAppProjectRunningSuccess(webadminGatewayConfig
                    , targetWebadminGatewayImage
                    , getDeployCluster()
                    , getK8sNamespace())
            this.checkAppProjectRunningSuccess(webadminServerConfig
                    , targetWebadminServerImage
                    , getDeployCluster()
                    , getK8sNamespace())
            this.checkAppProjectRunningSuccess(manageWebappConfig
                    , targetManageWebappImage
                    , getDeployCluster()
                    , getK8sNamespace())
        }

        List<String> notifyUserList = new ArrayList<>()
        notifyUserList.add(this.context.globalConfig.teamLeader)
        boolean verifySuccess = manualVerification(notifyUserList, true)
        if (verifySuccess) {
            this.processMerge(this.context.branchName, getTargetBranchNameAfterVerify())
            return
        }

        this.processFailed = true

        this.context.jenkins.stage("回滚阶段") {
            this.rollbackDeployImage(manageWebappConfig, originManageWebappImage, getArgoDeployEnv())
            this.rollbackDeployImage(webadminServerConfig, originWebadminServerImage, getArgoDeployEnv())
            this.rollbackDeployImage(webadminGatewayConfig, originWebadminGatewayImage, getArgoDeployEnv())
        }

        this.context.jenkins.stage("检测回滚") {
            this.checkAppProjectRunningSuccess(manageWebappConfig
                    , originManageWebappImage
                    , getDeployCluster()
                    , getK8sNamespace()
                    , true)
            this.checkAppProjectRunningSuccess(webadminServerConfig
                    , originWebadminServerImage
                    , getDeployCluster()
                    , getK8sNamespace()
                    , true)
            this.checkAppProjectRunningSuccess(webadminGatewayConfig
                    , originWebadminGatewayImage
                    , getDeployCluster()
                    , getK8sNamespace()
                    , true)
        }
    }
}
