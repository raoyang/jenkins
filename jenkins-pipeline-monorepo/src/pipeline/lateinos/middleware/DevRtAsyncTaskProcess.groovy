package pipeline.lateinos.middleware

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess
import pipeline.lateinos.LateinosProjectNames

/**
 * 异步任务框架开发流程
 */
class DevRtAsyncTaskProcess extends BaseProcess {

    protected DevRtAsyncTaskProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [LateinosProjectNames.RT_ASYNCTASK_CLEANER
                , LateinosProjectNames.RT_ASYNCTASK_DAO
                , LateinosProjectNames.RT_ASYNCTASK_SCHEDULER
                , LateinosProjectNames.RT_ASYNCTASK_SCHEDULER_WORKFLOW]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig daoItemConfig = this.context.projectsConfig.get(LateinosProjectNames.RT_ASYNCTASK_DAO)
        this.finishedNotifyCmd.addToNotifyUserList(daoItemConfig.projectMaintainer)
        String targetDaoImage = ""

        ProjectItemConfig cleanerItemConfig = this.context.projectsConfig.get(LateinosProjectNames.RT_ASYNCTASK_CLEANER)
        this.finishedNotifyCmd.addToNotifyUserList(cleanerItemConfig.projectMaintainer)
        String targetCleanerImage = ""

        ProjectItemConfig schedulerItemConfig = this.context.projectsConfig.get(LateinosProjectNames.RT_ASYNCTASK_SCHEDULER)
        this.finishedNotifyCmd.addToNotifyUserList(schedulerItemConfig.projectMaintainer)
        String targetSchedulerImage = ""

        ProjectItemConfig schedulerWorkflowItemConfig = this.context.projectsConfig.get(LateinosProjectNames.RT_ASYNCTASK_SCHEDULER_WORKFLOW)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(daoItemConfig, DeployCluster.UG_PUBLIC)
            this.projectConfigCheck(cleanerItemConfig, DeployCluster.UG_PUBLIC)
            this.projectConfigCheck(schedulerItemConfig, DeployCluster.UG_PUBLIC)
            this.projectConfigCheck(schedulerWorkflowItemConfig, DeployCluster.UG_PUBLIC)
        }

        this.context.jenkins.stage("代码扫描") {
            this.sonarCheck(daoItemConfig)
            this.sonarCheck(cleanerItemConfig)
            this.sonarCheck(schedulerItemConfig)
        }

        this.context.jenkins.stage("编译镜像") {
            targetDaoImage = this.buildAppProject(daoItemConfig)
            targetCleanerImage = this.buildAppProject(cleanerItemConfig)
            targetSchedulerImage = this.buildAppProject(schedulerItemConfig)
        }

        this.context.jenkins.stage("部署镜像集") {
            this.deployAppProjectYaml(daoItemConfig
                    , targetDaoImage
                    , "public"
                    , "lateinos")
            this.deployAppProjectYaml(cleanerItemConfig
                    , targetCleanerImage
                    , "public"
                    , "lateinos")
            this.deployAppProjectYaml(schedulerItemConfig
                    , targetSchedulerImage
                    , "public"
                    , "lateinos")
            this.deployAppProjectYaml(schedulerWorkflowItemConfig
                    , targetSchedulerImage
                    , "public"
                    , "lateinos")
        }

        this.context.jenkins.stage("检测系统运行") {
            this.checkAppProjectRunningSuccess(daoItemConfig
                    , targetDaoImage
                    , DeployCluster.UG_PUBLIC
                    , "lateinos")
            // 这是一个cronjob， 无法检测运行状态
//            this.checkAppProjectRunningSuccess(cleanerItemConfig
//                    , targetCleanerImage
//                    , DeployCluster.UG_PUBLIC
//                    , "lateinos")
            this.checkAppProjectRunningSuccess(schedulerItemConfig
                    , targetSchedulerImage
                    , DeployCluster.UG_PUBLIC
                    , "lateinos")
            this.checkAppProjectRunningSuccess(schedulerWorkflowItemConfig
                    , targetSchedulerImage
                    , DeployCluster.UG_PUBLIC
                    , "lateinos")
        }

    }
}
