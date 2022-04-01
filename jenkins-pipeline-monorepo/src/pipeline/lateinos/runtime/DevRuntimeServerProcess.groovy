package pipeline.lateinos.runtime

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess
import pipeline.lateinos.LateinosProjectNames

class DevRuntimeServerProcess extends BaseProcess {
    DevRuntimeServerProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [LateinosProjectNames.RUNTIME_SERVER]
    }

    @Override
    protected void execute() {
        finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig config = context.projectsConfig.get(LateinosProjectNames.RUNTIME_SERVER)
        finishedNotifyCmd.addToNotifyUserList(config.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(config, DeployCluster.UG_PUBLIC)
        }

        this.context.jenkins.stage("代码扫描") {
            sonarCheck(config)
        }

        String image = ""
        this.context.jenkins.stage("编译${config.projectName}") {
            image = buildAppProject(config)
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
