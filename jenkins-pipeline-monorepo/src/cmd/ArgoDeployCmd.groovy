package cmd

import cmd.utils.GitUtils
import cmd.utils.ImageVersionUtils

class ArgoDeployCmd extends AbstractCmd<Void> {

    // 部署信息
    private String projectName
    private String deployImageName
    private String targetImage
    private String deliveryGroup
    private String deployYamlFileName
    private String argocdGitUrl
    private String deployEnv

    // 用于代码生成
    private String codeType
    private String deployBaseTemplate
    private String deployNamespace
    private String deployReplicas = "1"

    // 注入部署yaml的变量定义
    private Map variables

    // 操作类型：ci-自动集成, rollback-回滚
    private String option = "ci"

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2

    ArgoDeployCmd(context) {
        super(context)
    }

    @Override
    Void getResult() {
        return null
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("projectName", this.projectName)
        if (!this.deployImageName) {
            this.deployImageName = this.projectName
        }
        checkEmpty("targetImage", this.targetImage)
        checkEmpty("deliveryGroup", this.deliveryGroup)
        checkEmpty("deployYamlFileName", this.deployYamlFileName)
        checkEmpty("argocdGitUrl", this.argocdGitUrl)
        checkEmpty("deployEnv", this.deployEnv)
        checkEmpty("codeType", this.codeType)
        checkEmpty("deployBaseTemplate", this.deployBaseTemplate)
        checkEmpty("deployNamespace", this.deployNamespace)

        String version = ImageVersionUtils.imageToTag(this.targetImage)
        String basePath = "/tmp/argocd/${this.deliveryGroup}/${version}"
        this.context.jenkins.sh "mkdir -p ${basePath}"
        this.context.jenkins.dir(basePath) {
            // 拉取配置代码
            GitUtils.pullGitRepository(this.context, this.argocdGitUrl, "main")

            // 更新相关GIT代码
            updateArgoGitFiles(version)

            // 提交部署配置yaml
            if (this.retryTimes > 0) {
                this.context.jenkins.retry(this.retryTimes + 1) {
                    GitUtils.pushGitRepository(this.context
                            , "main"
                            , "${this.option}(update argocd yaml image): projectName=${this.projectName}, deployImageName=${this.deployImageName}, targetImage=${this.targetImage}")
                }
            } else {
                GitUtils.pushGitRepository(this.context
                        , "main"
                        , "${this.option}(update argocd yaml image): projectName=${this.projectName}, deployImageName=${this.deployImageName}, targetImage=${this.targetImage}")
            }

            // TODO: 增加开关，触发自动Sync-argocd能力

        }

        return this
    }

    private void updateArgoGitFiles(String version) {
        // 环境目录不允许自动创建，需要人为介入
        String deployEnvDir = "${this.deliveryGroup}/${this.deployEnv}"
        def exists = this.context.jenkins.fileExists deployEnvDir
        if (!exists) {
            this.context.jenkins.error "Argocd-yaml文件目录不存在，请手动创建：${deployEnvDir}"
        }

        String deployProjectDir = "${this.deliveryGroup}/${this.deployEnv}/${this.projectName}"
        this.context.jenkins.sh "mkdir -p ${deployProjectDir}"

        this.context.jenkins.dir(deployProjectDir) {
            def deployFileExists = this.context.jenkins.fileExists this.deployYamlFileName
            if (!deployFileExists) {
                // 生成
                new GenerateDeployYamls(this.context)
                        .setVariables(this.variables)
                        .setTargetDir(".")
                        .setCodeType(this.codeType)
                        .setDeployBaseTemplate(this.deployBaseTemplate)
                        .setDeployImage(this.targetImage)
                        .setDeployName(this.projectName)
                        .setDeployNamespace(this.deployNamespace)
                        .setDeployReplicas(this.deployReplicas)
                        .execute()

                deployFileExists = this.context.jenkins.fileExists this.deployYamlFileName
                if (!deployFileExists) {
                    this.context.jenkins.error "GenerateDeployYamls execute, but do not generate ${this.deployYamlFileName}, please check config"
                }
            }

            // 修改镜像版本
            this.context.jenkins.sh """
                    sed -i 's/image: \\S\\+${this.deployImageName}:\\S\\+/image: ${
                this.targetImage.replace("/", "\\/")
            }/g' ${this.deployYamlFileName}
                """
            // 修正version
            this.context.jenkins.sh """sed -i 's/image-tag: \\S\\+/image-tag: ${version}/g' ${
                this.deployYamlFileName
            }"""

            // 提交GIT
            this.context.jenkins.sh "git add ."
        }
    }

    String getProjectName() {
        return projectName
    }

    ArgoDeployCmd setProjectName(String projectName) {
        this.projectName = projectName
        return this
    }

    String getTargetImage() {
        return targetImage
    }

    ArgoDeployCmd setTargetImage(String targetImage) {
        this.targetImage = targetImage
        return this
    }

    String getDeliveryGroup() {
        return deliveryGroup
    }

    ArgoDeployCmd setDeliveryGroup(String deliveryGroup) {
        this.deliveryGroup = deliveryGroup
        return this
    }

    String getDeployYamlFileName() {
        return deployYamlFileName
    }

    ArgoDeployCmd setDeployYamlFileName(String deployYamlFileName) {
        this.deployYamlFileName = deployYamlFileName
        return this
    }

    String getArgocdGitUrl() {
        return argocdGitUrl
    }

    ArgoDeployCmd setArgocdGitUrl(String argocdGitUrl) {
        this.argocdGitUrl = argocdGitUrl
        return this
    }

    String getDeployEnv() {
        return deployEnv
    }

    ArgoDeployCmd setDeployEnv(String deployEnv) {
        this.deployEnv = deployEnv
        return this
    }

    String getCodeType() {
        return codeType
    }

    ArgoDeployCmd setCodeType(String codeType) {
        this.codeType = codeType
        return this
    }

    String getDeployBaseTemplate() {
        return deployBaseTemplate
    }

    ArgoDeployCmd setDeployBaseTemplate(String deployBaseTemplate) {
        this.deployBaseTemplate = deployBaseTemplate
        return this
    }

    String getDeployNamespace() {
        return deployNamespace
    }

    ArgoDeployCmd setDeployNamespace(String deployNamespace) {
        this.deployNamespace = deployNamespace
        return this
    }

    String getDeployReplicas() {
        return deployReplicas
    }

    ArgoDeployCmd setDeployReplicas(String deployReplicas) {
        this.deployReplicas = deployReplicas
        return this
    }

    int getRetryTimes() {
        return retryTimes
    }

    ArgoDeployCmd setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes
        return this
    }

    String getOption() {
        return option
    }

    ArgoDeployCmd setOption(String option) {
        this.option = option
        return this
    }

    Map getVariables() {
        return variables
    }

    ArgoDeployCmd setVariables(Map variables) {
        this.variables = variables
        return this
    }

    String getDeployImageName() {
        return deployImageName
    }

    ArgoDeployCmd setDeployImageName(String deployImageName) {
        this.deployImageName = deployImageName
        return this
    }
}
