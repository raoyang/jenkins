package pipeline.ugoperation

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

class DevProcess extends BaseProcess{

    static final String DEPLOY_NAMESPACE = "UG-OPERATION"
    static final String DEPLOY_ENV = "dev"

    DevProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [UgOperationProjectNames.UG_BOSS_SERVER,
                UgOperationProjectNames.UG_OPERATION_TOOL_SERVER,
                UgOperationProjectNames.UG_BOSS_WEBAPP]
    }

    @Override
    protected void execute() {
       this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        Map originImages = [:]  // 原始镜像
        Map newImages = [:] //新编译的镜像

        List<String> projectNames = careProjects();

        this.context.jenkins.stage("配置检查") {
            for (String projectName : projectNames){
                this.projectConfigCheck(this.context.projectsConfig.get(projectName),DeployCluster.UG_DEV)
            }
        }

        this.context.jenkins.stage("保留现有镜像"){
            for(String projectName : projectNames){
                originImages[projectName] = this.getDeployImage(this.context.projectsConfig.get(projectName),DEPLOY_ENV)
            }
        }

        this.context.jenkins.stage("代码扫描") {
            for(String projectName : projectNames){
//                this.sonarCheck(this.context.projectsConfig.get(projectName))
            }
        }

        this.context.jenkins.stage("编译镜像") {
            for(String projectName : projectNames){
                newImages[projectName] = this.buildAppProject(this.context.projectsConfig.get(projectName))
            }
        }

        this.context.jenkins.stage("部署并检查运行状态") {
            for (String projectName : projectNames){
                ProjectItemConfig itemConfig = this.context.projectsConfig.get(projectName);
                this.deployAppProjectYaml(itemConfig, newImages[projectName].toString(), DEPLOY_ENV,DEPLOY_NAMESPACE)
//                this.checkAppProjectRunningSuccess(itemConfig,newImages[projectName].toString(),DeployCluster.UG_DEV,DEPLOY_NAMESPACE)
            }
        }

//        List<String> notifyUserList = new ArrayList<>()
//        notifyUserList.add(this.context.globalConfig.teamLeader)
//        boolean verifySuccess = manualVerification(notifyUserList, true)
//        if (verifySuccess) {
//            this.processMerge(this.context.branchName, "dev")
//            return
//        }
//
//        // 发布失败，全部回滚，标记流水线失败
//        this.processFailed = true
//        this.context.jenkins.stage("回滚镜像"){
//            for (int index = projectNames.size() - 1; index >= 0; index--) {
//                String projectName = projectNames.get(index);
//                this.rollbackDeployImage(this.context.projectsConfig.get(projectName),originImages[projectName].toString(),DEPLOY_ENV)
//            }
//        }
//
//        this.context.jenkins.stage("检测回滚"){
//            for (int index = projectNames.size() - 1; index >= 0; index--) {
//                String projectName = projectNames.get(index)
//                this.checkAppProjectRunningSuccess(this.context.projectsConfig.get(projectName),originImages[projectName].toString(),DeployCluster.UG_DEV,DEPLOY_NAMESPACE,true)
//            }
//        }
    }
}
