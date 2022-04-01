package pipeline.athena

import entity.Context
import enums.DeployCluster
import pipeline.BaseProcess

/**
 *  webadmin公共集群的发布流程
 */
class PublishWebadminProcess extends BaseProcess {

    PublishWebadminProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [
            AthenaProjectNames.WEBADMIN_GATEWAY
            , AthenaProjectNames.WEBADMIN_SERVER
            , AthenaProjectNames.MANAGE_WEBAPP
            , AthenaProjectNames.SWAGGER_WEBAPP
            , AthenaProjectNames.SWAGGER_PROXY
        ]
    }

    private static DeployCluster getDeployCluster() {
        return DeployCluster.UG_PUBLIC
    }

    private static String getVirtualEnv() {
        return null
    }

    private static String getArgoDeployEnv() {
        return "public"
    }

    private static String getK8sNamespace() {
        return "athena"
    }

    @Override
    protected void execute() {
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
                targetImages[projectName] = this.buildAppProject(this.context.projectsConfig.get(projectName))
            }
        }

        this.context.jenkins.stage("发布镜像&检测运行") {
            for (String projectName : careProjects()) {
                this.deployAppProjectYaml(this.context.projectsConfig.get(projectName)
                        , targetImages[projectName].toString()
                        , getArgoDeployEnv()
                        , getK8sNamespace())

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
            this.processMerge(this.context.branchName, "main")
            return
        }

        // 发布失败，全部回滚，标记流水线失败
        this.processFailed = true

        List<String> projectNames = careProjects();
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
                this.checkAppProjectRunningSuccess(this.context.projectsConfig.get(projectName)
                        , originImages[projectName].toString()
                        , getDeployCluster()
                        , getK8sNamespace()
                        , true)
            }
        }
    }
}
