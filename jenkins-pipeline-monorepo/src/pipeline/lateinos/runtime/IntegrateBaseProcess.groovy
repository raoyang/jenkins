package pipeline.lateinos.runtime

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess
import pipeline.lateinos.LateinosProjectNames

/**
 *  集成流程的积累
 */
abstract class IntegrateBaseProcess extends BaseProcess {
    protected IntegrateBaseProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [LateinosProjectNames.CONTROLLER_AGENT,
                LateinosProjectNames.API_GATEWAY,
                LateinosProjectNames.RUNTIME_SERVER,
                LateinosProjectNames.API_SERVER]
    }

    abstract protected DeployCluster getDeployCluster()
    abstract protected String getArgoDeployEnv()
    abstract protected String getK8sNamespace()
    abstract protected String getTargetBranchNameAfterVerify()

    @Override
    protected void execute() {
        ProjectItemConfig controllerAgentConfig = this.context.projectsConfig.get(LateinosProjectNames.CONTROLLER_AGENT)
        String originControllerAgentImage = ""
        String targetControllerAgentImage = ""

        ProjectItemConfig apiGatewayConfig = this.context.projectsConfig.get(LateinosProjectNames.API_GATEWAY)
        String originApiGatewayImage = ""
        String targetApiGatewayImage = ""

        ProjectItemConfig runtimeServerConfig = this.context.projectsConfig.get(LateinosProjectNames.RUNTIME_SERVER)
        String originRuntimeServerImage = ""
        String targetRuntimeServerImage = ""

        ProjectItemConfig apiServerConfig = this.context.projectsConfig.get(LateinosProjectNames.API_SERVER)
        String originApiServerImage = ""
        String targetApiServerImage = ""

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(controllerAgentConfig, getDeployCluster())
            this.projectConfigCheck(apiGatewayConfig, getDeployCluster())
            this.projectConfigCheck(runtimeServerConfig, getDeployCluster())
            this.projectConfigCheck(apiServerConfig, getDeployCluster())
        }

        this.context.jenkins.stage("编译镜像集") {
            targetControllerAgentImage = this.buildAppProject(controllerAgentConfig)
            targetApiGatewayImage = this.buildAppProject(apiGatewayConfig)
            targetRuntimeServerImage = this.buildAppProject(runtimeServerConfig)
            targetApiServerImage = this.buildAppProject(apiServerConfig)
        }

        this.context.jenkins.stage("保存部署信息") {
            originControllerAgentImage = this.getDeployImage(controllerAgentConfig, getArgoDeployEnv())
            originApiGatewayImage = this.getDeployImage(apiGatewayConfig, getArgoDeployEnv())
            originRuntimeServerImage = this.getDeployImage(runtimeServerConfig, getArgoDeployEnv())
            originApiServerImage = this.getDeployImage(apiServerConfig, getArgoDeployEnv())
        }

        this.context.jenkins.stage("提交部署YAML集") {
            this.deployAppProjectYaml(controllerAgentConfig
                    , targetControllerAgentImage
                    , getArgoDeployEnv()
                    , getK8sNamespace())
            this.deployAppProjectYaml(apiGatewayConfig
                    , targetApiGatewayImage
                    , getArgoDeployEnv()
                    , getK8sNamespace())
            this.deployAppProjectYaml(runtimeServerConfig
                    , targetRuntimeServerImage
                    , getArgoDeployEnv()
                    , getK8sNamespace())
            this.deployAppProjectYaml(apiServerConfig
                    , targetApiServerImage
                    , getArgoDeployEnv()
                    , getK8sNamespace())
        }

        this.context.jenkins.stage("检测部署") {
            this.checkAppProjectRunningSuccess(controllerAgentConfig
                    , targetControllerAgentImage
                    , getDeployCluster()
                    , getK8sNamespace())
            this.checkAppProjectRunningSuccess(apiGatewayConfig
                    , targetApiGatewayImage
                    , getDeployCluster()
                    , getK8sNamespace())
            this.checkAppProjectRunningSuccess(runtimeServerConfig
                    , targetRuntimeServerImage
                    , getDeployCluster()
                    , getK8sNamespace())
            this.checkAppProjectRunningSuccess(apiServerConfig
                    , targetApiServerImage
                    , getDeployCluster()
                    , getK8sNamespace())
        }

        List<String> notifyUserList = new ArrayList<>()
        notifyUserList.add(this.context.globalConfig.teamLeader)
        boolean verifySuccess = manualVerification(notifyUserList)
        if (verifySuccess) {
            this.processMerge(this.context.getBranchName(), getTargetBranchNameAfterVerify())
            return
        }

        this.context.jenkins.stage("回滚阶段") {
            this.rollbackDeployImage(apiServerConfig, originApiServerImage, getArgoDeployEnv())
            this.rollbackDeployImage(runtimeServerConfig, originRuntimeServerImage, getArgoDeployEnv())
            this.rollbackDeployImage(apiGatewayConfig, originApiGatewayImage, getArgoDeployEnv())
            this.rollbackDeployImage(controllerAgentConfig, originControllerAgentImage, getArgoDeployEnv())
        }

        this.context.jenkins.stage("检测回滚") {
            this.checkAppProjectRunningSuccess(apiServerConfig
                    , originApiServerImage
                    , getDeployCluster()
                    , getK8sNamespace()
                    , true)
            this.checkAppProjectRunningSuccess(runtimeServerConfig
                    , originRuntimeServerImage
                    , getDeployCluster()
                    , getK8sNamespace()
                    , true)
            this.checkAppProjectRunningSuccess(apiGatewayConfig
                    , originApiGatewayImage
                    , getDeployCluster()
                    , getK8sNamespace()
                    , true)
            this.checkAppProjectRunningSuccess(controllerAgentConfig
                    , originControllerAgentImage
                    , getDeployCluster()
                    , getK8sNamespace()
                    , true)
        }
    }
}
