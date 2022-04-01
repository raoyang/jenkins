package pipeline.ugengine

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

class DevHaProcess extends BaseProcess{
    DevHaProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [UgEngineProjectNames.ENGINE_HA_CONTROLLER]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig config_ha_controller = context.projectsConfig.get(UgEngineProjectNames.ENGINE_HA_CONTROLLER)

        this.finishedNotifyCmd.addToNotifyUserList(config_ha_controller.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(config_ha_controller, DeployCluster.UG_DEV)
        }

        this.context.jenkins.stage("代码扫描") {
            //sonarCheck(config_ha_controller)
        }

        //编译
        String image_ha_controller = buildAppProjectInner(config_ha_controller)

        //部署
        deployAppProjectInner(config_ha_controller, image_ha_controller)
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
