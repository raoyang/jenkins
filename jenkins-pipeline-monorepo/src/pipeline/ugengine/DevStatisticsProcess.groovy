package pipeline.ugengine

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

/**
 *  Engine-Statistics开发流程流水线
 */
class DevStatisticsProcess extends BaseProcess {
    DevStatisticsProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [UgEngineProjectNames.ENGINE_STATISTICS,UgEngineProjectNames.ENGINE_STATISTICS_ACTIVE_CORP_COLLECTOR]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig config_statistics = context.projectsConfig.get(UgEngineProjectNames.ENGINE_STATISTICS)
        //ProjectItemConfig config_active_corp_collector = context.projectsConfig.get(UgEngineProjectNames.ENGINE_STATISTICS_ACTIVE_CORP_COLLECTOR)

        this.finishedNotifyCmd.addToNotifyUserList(config_statistics.projectMaintainer)
        //this.finishedNotifyCmd.addToNotifyUserList(config_active_corp_collector.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(config_statistics, DeployCluster.UG_DEV)
            //this.projectConfigCheck(config_active_corp_collector, DeployCluster.UG_DEV)
        }

        this.context.jenkins.stage("代码扫描") {
//            sonarCheck(config_statistics)
//            sonarCheck(config_active_corp_collector)
        }

         String image_statistics = ""
         this.context.jenkins.stage("编译镜像${config_statistics.projectName}") {
             image_statistics = this.buildAppProject(config_statistics)
         }

//         String image_active_corp_collector = ""
//         this.context.jenkins.stage("编译镜像${config_active_corp_collector.projectName}") {
//            image_active_corp_collector = this.buildAppProject(config_active_corp_collector)
//         }

         this.context.jenkins.stage("部署${config_statistics.projectName}") {
             this.deployAppProjectYaml(config_statistics
                     , image_statistics
                     , "dev"
                     , "ug-engine")
        }

//        this.context.jenkins.stage("部署${config_active_corp_collector.projectName}") {
//            this.deployAppProjectYaml(config_active_corp_collector
//                    , image_active_corp_collector
//                    , "dev"
//                    , "ug-engine")
//        }

    }
}
