package pipeline.lateinos.middleware

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess
import pipeline.lateinos.LateinosProjectNames

/**
 *  开发xxljob的流程
 */
class DevXxlJobProcess extends BaseProcess {

    DevXxlJobProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [LateinosProjectNames.XXLJOB]
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
        return "lateinos"
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        Map targetImages = [:]

        this.context.jenkins.stage("检测配置") {
            for (String projectName : careProjects()) {
                this.projectConfigCheck(this.getItemConfig(projectName), getDeployCluster(), getVirtualEnv())
            }
        }

        this.context.jenkins.stage("编译镜像") {
            for (String projectName : careProjects()) {
                targetImages[projectName] = this.buildAppProject(this.getItemConfig(projectName))
            }
        }

        this.context.jenkins.stage("发布镜像") {
            for (String projectName : careProjects()) {
                this.deployAppProjectYaml(this.getItemConfig(projectName)
                        , targetImages[projectName].toString()
                        , getArgoDeployEnv()
                        , getK8sNamespace())
            }
        }

        this.context.jenkins.stage("检测运行") {
            for (String projectName : careProjects()) {
                this.checkAppProjectRunningSuccess(this.getItemConfig(projectName)
                        , targetImages[projectName].toString()
                        , getDeployCluster()
                        , getK8sNamespace())
            }
        }
    }
}
