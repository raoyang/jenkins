package pipeline.lateinos.runtime

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess
import pipeline.lateinos.LateinosProjectNames

/**
 *  开发Gateway的流程
 */
class DevGatewayProcess extends BaseProcess {
    DevGatewayProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [LateinosProjectNames.API_GATEWAY]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig config = context.projectsConfig.get(LateinosProjectNames.API_GATEWAY)
        this.finishedNotifyCmd.addToNotifyUserList(config.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(config, DeployCluster.UG_PUBLIC)
        }

        this.context.jenkins.stage("代码扫描") {
            sonarCheck(config)
        }

        String image = ""
        this.context.jenkins.stage("编译镜像${config.projectName}") {
            image = this.buildAppProject(config)
        }

        this.context.jenkins.stage("部署${config.projectName}") {
            this.deployAppProjectYaml(config
                    , image
                    , "public"
                    , "lateinos")

            this.checkAppProjectRunningSuccess(config
                    , image
                    , DeployCluster.UG_PUBLIC
                    , "lateinos")
        }
    }
}
