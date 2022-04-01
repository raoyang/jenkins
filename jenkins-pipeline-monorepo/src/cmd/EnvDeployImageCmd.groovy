package cmd

import cmd.utils.GitUtils

import java.text.SimpleDateFormat

/**
 * 获取当前发布镜像版本号
 */
class EnvDeployImageCmd extends AbstractCmd<String> {

    private String projectName
    private String deployImageName
    private String deliveryGroup
    private String deployYamlFileName
    private String argocdGitUrl
    private String deployEnv

    private String resultImpl

    EnvDeployImageCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("projectName", this.projectName)
        if (!this.deployImageName) {
            this.deployImageName = this.projectName
        }
        checkEmpty("deliveryGroup", this.deliveryGroup)
        checkEmpty("deployYamlFileName", this.deployYamlFileName)
        checkEmpty("argocdGitUrl", this.argocdGitUrl)
        checkEmpty("deployEnv", this.deployEnv)

        String time = new SimpleDateFormat("yyyyMMddHHmm")
                .format(new Date(this.context.jenkins.currentBuild.startTimeInMillis))
        String basePath = "/tmp/argocd/${this.deliveryGroup}/current/${time}"
        this.context.jenkins.sh "mkdir -p ${basePath}"
        this.context.jenkins.dir(basePath) {
            // 拉取配置代码
            GitUtils.pullGitRepository(this.context, this.argocdGitUrl, "main")

            // 查询当前版本号
            String deployProjectDir = "${this.deliveryGroup}/${this.deployEnv}/${this.projectName}"
            this.context.jenkins.sh "mkdir -p ${deployProjectDir}"
            this.context.jenkins.dir(deployProjectDir) {
                def deployFileExists = this.context.jenkins.fileExists this.deployYamlFileName
                if (deployFileExists) {
                    String image = this.context.jenkins.sh(script: """
                        grep 'image: \\S\\+${this.deployImageName}:\\S\\+' ${this.deployYamlFileName}
                    """, returnStdout: true).trim()

                    if (image != null && image.size() > 0) {
                        this.resultImpl = image.substring(7, image.size())
                        this.context.jenkins.echo "当前发布镜像: projectName=${this.projectName}, deployImageName=${this.deployImageName}, currentDeployVersion=${this.resultImpl}"
                    } else {
                        this.context.jenkins.error "无法获取项目${this.projectName}的镜像${this.deployImageName}在${this.deployEnv}的发布版本"
                    }
                } else {
                    resultImpl = ""
                }
            }
        }

        return this
    }

    @Override
    String getResult() {
        return this.resultImpl
    }

    String getProjectName() {
        return projectName
    }

    EnvDeployImageCmd setProjectName(String projectName) {
        this.projectName = projectName
        return this
    }

    String getDeployImageName() {
        return deployImageName
    }

    EnvDeployImageCmd setDeployImageName(String deployImageName) {
        this.deployImageName = deployImageName
        return this
    }

    String getDeliveryGroup() {
        return deliveryGroup
    }

    EnvDeployImageCmd setDeliveryGroup(String deliveryGroup) {
        this.deliveryGroup = deliveryGroup
        return this
    }

    String getDeployYamlFileName() {
        return deployYamlFileName
    }

    EnvDeployImageCmd setDeployYamlFileName(String deployYamlFileName) {
        this.deployYamlFileName = deployYamlFileName
        return this
    }

    String getArgocdGitUrl() {
        return argocdGitUrl
    }

    EnvDeployImageCmd setArgocdGitUrl(String argocdGitUrl) {
        this.argocdGitUrl = argocdGitUrl
        return this
    }

    String getDeployEnv() {
        return deployEnv
    }

    EnvDeployImageCmd setDeployEnv(String deployEnv) {
        this.deployEnv = deployEnv
        return this
    }
}
