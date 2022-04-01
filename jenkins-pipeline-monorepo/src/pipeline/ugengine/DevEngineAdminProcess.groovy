package pipeline.ugengine

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

class DevEngineAdminProcess extends BaseProcess {
    DevEngineAdminProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [UgEngineProjectNames.ENGINE_ADMIN_WEBAPI,
                UgEngineProjectNames.ENGINE_ADMIN_WEBAPP,
                UgEngineProjectNames.ENGINE_ADMIN_UPGRADE]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig config_admin_webapi = context.projectsConfig.get(UgEngineProjectNames.ENGINE_ADMIN_WEBAPI)
        ProjectItemConfig config_admin_webapp = context.projectsConfig.get(UgEngineProjectNames.ENGINE_ADMIN_WEBAPP)
        ProjectItemConfig config_admin_upgrade = context.projectsConfig.get(UgEngineProjectNames.ENGINE_ADMIN_UPGRADE)

        this.finishedNotifyCmd.addToNotifyUserList(config_admin_webapi.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(config_admin_webapi, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_admin_upgrade, DeployCluster.UG_DEV)
        }

        this.context.jenkins.stage("代码扫描") {
            sonarCheck(config_admin_webapi)
            sonarCheck(config_admin_upgrade)
        }

        String image_admin_webapi = buildAppProjectInner(config_admin_webapi)
        String image_admin_webapp = buildAppProjectInner(config_admin_webapp)
        String image_admin_upgrade = buildAppProjectInner(config_admin_upgrade)

        this.context.jenkins.stage("部署") {
            if(image_admin_webapi.length() > 0) {
                this.deployAppProjectYaml(config_admin_webapi
                        , image_admin_webapi
                        , "dev"
                        , "h3yun-engine")
            }
            if(image_admin_webapp.length() > 0) {
                this.deployAppProjectYaml(config_admin_webapp
                        , image_admin_webapp
                        , "dev"
                        , "h3yun-engine")
            }

            if(image_admin_upgrade.length() > 0) {
                //dotnet当前没有cronjob的部署模板，先手动更新
//                this.deployAppProjectYaml(config_admin_upgrade
//                        , image_admin_upgrade
//                        , "dev"
//                        , "h3yun-engine")
            }
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
}
