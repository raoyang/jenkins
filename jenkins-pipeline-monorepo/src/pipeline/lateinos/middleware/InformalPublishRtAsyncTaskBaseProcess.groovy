package pipeline.lateinos.middleware

import entity.Context
import enums.DeployCluster
import pipeline.BaseProcess
import pipeline.lateinos.LateinosProjectNames

/**
 *  异步任务框架非正式发布流程
 */
abstract class InformalPublishRtAsyncTaskBaseProcess extends BaseProcess{
    protected InformalPublishRtAsyncTaskBaseProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [
            LateinosProjectNames.RT_ASYNCTASK_DAO
            , LateinosProjectNames.RT_ASYNCTASK_SCHEDULER
            , LateinosProjectNames.RT_ASYNCTASK_SCHEDULER_WORKFLOW
            , LateinosProjectNames.RT_ASYNCTASK_CLEANER
        ]
    }

    protected abstract DeployCluster getDeployCluster()
    protected abstract String getVirtualEnv()
    protected abstract String getArgoDeployEnv()
    protected abstract String getK8sNamespace()
    protected abstract String getTargetBranchAfterVerify()

    @Override
    protected void execute() {
        this.finishedNotifyCmd.setNotifyToGlobalFlag(true)

        this.finishedNotifyCmd.setNotifyToGlobalFlag(true)

        Map targetImages = [:]
        Map originImages = [:]

        this.context.jenkins.stage("配置检查") {
            for (String projectName : careProjects()) {
                this.projectConfigCheck(this.context.projectsConfig.get(projectName)
                        , getDeployCluster()
                        , getVirtualEnv())
            }
        }

        this.context.jenkins.stage("保存现场") {
            for (String projectName : careProjects()) {
                originImages[projectName] = this.getDeployImage(this.context.projectsConfig.get(projectName), getArgoDeployEnv())
            }
        }

        this.context.jenkins.stage("编译镜像") {
            for (String projectName : careProjects()) {
                if (projectName.equalsIgnoreCase(LateinosProjectNames.RT_ASYNCTASK_SCHEDULER_WORKFLOW)) {
                    targetImages[projectName] = targetImages[LateinosProjectNames.RT_ASYNCTASK_SCHEDULER]
                } else {
                    targetImages[projectName] = this.buildAppProject(this.context.projectsConfig.get(projectName))
                }
            }
        }

        this.context.jenkins.stage("发布镜像&检测运行") {
            for (String projectName : careProjects()) {
                this.deployAppProjectYaml(this.context.projectsConfig.get(projectName)
                        , targetImages[projectName].toString()
                        , getArgoDeployEnv()
                        , getK8sNamespace())

                // cronjob 无须检测，跳过
                if (projectName.equalsIgnoreCase(LateinosProjectNames.RT_ASYNCTASK_CLEANER)) {
                    continue
                }

                this.checkAppProjectRunningSuccess(this.context.projectsConfig.get(projectName)
                        , targetImages[projectName].toString()
                        , getDeployCluster()
                        , getK8sNamespace())
            }
        }

        List<String> notifyUserList = new ArrayList<>()
        notifyUserList.add(this.context.globalConfig.teamLeader)
        boolean verifySuccess = manualVerification(notifyUserList, true)
        if (verifySuccess) {
            this.processMerge(this.context.branchName, getTargetBranchAfterVerify())
            return
        }

        // 发布失败，全部回滚，标记流水线失败
        this.processFailed = true

        List<String> projectNames = careProjects()
        this.context.jenkins.stage("回滚镜像") {
            for (int index = projectNames.size() - 1; index >= 0; --index) {
                String projectName = projectNames.get(index)
                this.rollbackDeployImage(this.context.projectsConfig.get(projectName)
                        , originImages[projectName].toString()
                        , getArgoDeployEnv())
            }
        }

        this.context.jenkins.stage("检测回滚") {
            for (int index = projectNames.size() - 1; index >= 0; --index) {
                String projectName = projectNames.get(index)

                if (projectName.equalsIgnoreCase(LateinosProjectNames.RT_ASYNCTASK_CLEANER)) {
                    continue
                }

                this.checkAppProjectRunningSuccess(this.context.projectsConfig.get(projectName)
                        , originImages[projectName].toString()
                        , getDeployCluster()
                        , getK8sNamespace()
                        , true)
            }
        }
    }
}
