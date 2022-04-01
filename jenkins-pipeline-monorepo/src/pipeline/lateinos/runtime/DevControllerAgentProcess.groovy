package pipeline.lateinos.runtime

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess
import pipeline.lateinos.LateinosProjectNames

class DevControllerAgentProcess extends BaseProcess{

    DevControllerAgentProcess(Context context) {
        super(context)
    }

    @Override
    def careProjects() {
        return [LateinosProjectNames.CONTROLLER_AGENT]
    }

    @Override
    void execute() {
        finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig controllerAgentConfig = context.projectsConfig.get(LateinosProjectNames.CONTROLLER_AGENT)
        finishedNotifyCmd.addToNotifyUserList(controllerAgentConfig.projectMaintainer)

//        this.context.jenkins.stage("配置检查") {
//            this.projectConfigCheck(controllerAgentConfig, DeployCluster.UG_PUBLIC)
//        }

        this.context.jenkins.stage("代码扫描") {
            sonarCheck(controllerAgentConfig)
        }

        String controllerAgentImage = ""
        this.context.jenkins.stage("编译镜像${controllerAgentConfig.projectName}") {
            controllerAgentImage = this.buildAppProject(controllerAgentConfig)
        }

        this.context.jenkins.stage("部署${controllerAgentConfig.projectName}") {
            this.deployAppProjectYaml(controllerAgentConfig
                    , controllerAgentImage
                    , "public"
                    , "lateinos")

            this.checkAppProjectRunningSuccess(controllerAgentConfig
                    , controllerAgentImage
                    , DeployCluster.UG_PUBLIC
                    , "lateinos")
        }
    }

}
