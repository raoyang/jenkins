package pipeline.ugengine

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

class DevAppIntegrationProcess extends BaseProcess {
    DevAppIntegrationProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [UgEngineProjectNames.ENGINE_APP_INTEGRATION_DISPATCHER]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig config_app_integration_dispatcher = context.projectsConfig.get(UgEngineProjectNames.ENGINE_APP_INTEGRATION_DISPATCHER)

        this.finishedNotifyCmd.addToNotifyUserList(config_app_integration_dispatcher.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(config_app_integration_dispatcher, DeployCluster.UG_DEV)
        }

        this.context.jenkins.stage("代码扫描") {
            sonarCheck(config_app_integration_dispatcher)
        }

        String image_app_integration_dispatcher = ""

        this.context.jenkins.stage("编译镜像${config_app_integration_dispatcher.projectName}") {
            image_app_integration_dispatcher = this.buildAppProject(config_app_integration_dispatcher)
        }

        this.context.jenkins.stage("部署${config_app_integration_dispatcher.projectName}") {
            this.deployAppProjectYaml(config_app_integration_dispatcher
                    , image_app_integration_dispatcher
                    , "dev"
                    , "h3yun-engine")
        }
    }
}
