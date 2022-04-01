package pipeline.ugengine

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

/**
 *  Engine-Workflow开发流程流水线
 */
class DevWorkflowProcess extends BaseProcess{
    DevWorkflowProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [UgEngineProjectNames.ENGINE_WORKFLOW_SCHEDULER
                ,UgEngineProjectNames.ENGINE_WORKFLOW_TASK_EXECUTOR
                ,UgEngineProjectNames.ENGINE_WORKFLOW_TASK_EXECUTOR_CHRONOS
                ,UgEngineProjectNames.ENGINE_WORKFLOW_WEBAPI]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig config_workflow_scheduler = context.projectsConfig.get(UgEngineProjectNames.ENGINE_WORKFLOW_SCHEDULER)
        ProjectItemConfig config_workflow_task_executor = context.projectsConfig.get(UgEngineProjectNames.ENGINE_WORKFLOW_TASK_EXECUTOR)
        ProjectItemConfig config_workflow_task_executor_chronos = context.projectsConfig.get(UgEngineProjectNames.ENGINE_WORKFLOW_TASK_EXECUTOR_CHRONOS)
        ProjectItemConfig config_workflow_webapi = context.projectsConfig.get(UgEngineProjectNames.ENGINE_WORKFLOW_WEBAPI)

        this.finishedNotifyCmd.addToNotifyUserList(config_workflow_scheduler.projectMaintainer)
        this.finishedNotifyCmd.addToNotifyUserList(config_workflow_task_executor.projectMaintainer)
        this.finishedNotifyCmd.addToNotifyUserList(config_workflow_task_executor_chronos.projectMaintainer)
        this.finishedNotifyCmd.addToNotifyUserList(config_workflow_webapi.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(config_workflow_scheduler, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_workflow_task_executor, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_workflow_task_executor_chronos, DeployCluster.UG_DEV)
            this.projectConfigCheck(config_workflow_webapi, DeployCluster.UG_DEV)
        }

        this.context.jenkins.stage("代码扫描") {
            sonarCheck(config_workflow_scheduler)
            sonarCheck(config_workflow_task_executor)
            sonarCheck(config_workflow_task_executor_chronos)
            sonarCheck(config_workflow_webapi)
        }

        String image_workflow_scheduler = buildAppProjectInner(config_workflow_scheduler)
        String image_workflow_task_executor = buildAppProjectInner(config_workflow_task_executor)
        String image_workflow_task_executor_chronos = buildAppProjectInner(config_workflow_task_executor_chronos)
        String image_workflow_webapi = buildAppProjectInner(config_workflow_webapi)

        deployAppProjectInner(config_workflow_scheduler, image_workflow_scheduler)
        deployAppProjectInner(config_workflow_task_executor, image_workflow_task_executor)
        deployAppProjectInner(config_workflow_task_executor_chronos, image_workflow_task_executor_chronos)
        deployAppProjectInner(config_workflow_webapi, image_workflow_webapi)
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
