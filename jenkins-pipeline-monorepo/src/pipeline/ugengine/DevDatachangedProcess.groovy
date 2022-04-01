package pipeline.ugengine

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import org.apache.tools.ant.Project
import pipeline.BaseProcess

/**
 *  engine-datachanged开发流程流水线
 */
class DevDatachangedProcess extends BaseProcess {
    DevDatachangedProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [UgEngineProjectNames.ENGINE_DATACHANGED_HISTORY,UgEngineProjectNames.ENGINE_DATACHANGED_DISPATCHER]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig config_history = context.projectsConfig.get(UgEngineProjectNames.ENGINE_DATACHANGED_HISTORY)
        ProjectItemConfig config_dispatcher = context.projectsConfig.get(UgEngineProjectNames.ENGINE_DATACHANGED_DISPATCHER)

        this.finishedNotifyCmd.addToNotifyUserList(config_history.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(config_history, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_dispatcher, DeployCluster.UG_DEV)
        }

        this.context.jenkins.stage("代码扫描") {
            sonarCheck(config_history)
            sonarCheck(config_dispatcher)
        }

        String image_history = buildAppProjectInner(config_history)
        String image_dispatcher = buildAppProjectInner(config_dispatcher)

        deployAppProjectInner(config_history, image_history)
        deployAppProjectInner(config_dispatcher, image_dispatcher)

        this.context.jenkins.stage("部署${config_dispatcher.projectName}") {
            this.deployAppProjectYaml(config_dispatcher
                    , image_dispatcher
                    , "dev"
                    , "h3yun-engine")

            this.checkAppProjectRunningSuccess(config_dispatcher,
                    image_dispatcher,
                    DeployCluster.UG_DEV,
                    "h3yun-engine")
        }
    }

    private String buildAppProjectInner(ProjectItemConfig config){
        String image_url = ""
        if(!config.needBuild) {
            return image_url
        }
        this.context.jenkins.stage("编译镜像${config.projectName}") {
            image_url = this.buildAppProject(config)
        }
        return image_url
    }

    private void deployAppProjectInner(ProjectItemConfig config, String image_url) {
        if(!config.needBuild) {
            return
        }
        this.context.jenkins.stage("部署${config.projectName}") {
            this.deployAppProjectYaml(config
                    , image_url
                    , "dev"
                    , "ug-engine")
        }
    }
}
