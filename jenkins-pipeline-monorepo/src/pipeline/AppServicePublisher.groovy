package pipeline

import cmd.BuildNotifyCmd
import cmd.InputMessageCmd
import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster

/**
 *  后台单个应用的发布子流程
 */
class AppServicePublisher {
    private ProjectItemConfig itemConfig

    private String targetImage
    private String targetDeployEnv

    private String targetDeployNamespace
    private DeployCluster targetDeployCluster
    private int defaultReplicas = 2

    private String originImage
    private boolean needPublish = true

    private BaseProcess baseProcess
    private Context context

    AppServicePublisher(Context context, BaseProcess baseProcess) {
        this.context = context
        this.baseProcess = baseProcess
    }

    ProjectItemConfig getItemConfig() {
        return itemConfig
    }

    AppServicePublisher setItemConfig(ProjectItemConfig itemConfig) {
        this.itemConfig = itemConfig
        return this
    }

    String getTargetImage() {
        return targetImage
    }

    AppServicePublisher setTargetImage(String targetImage) {
        this.targetImage = targetImage
        return this
    }

    String getTargetDeployEnv() {
        return targetDeployEnv
    }

    AppServicePublisher setTargetDeployEnv(String targetDeployEnv) {
        this.targetDeployEnv = targetDeployEnv
        return this
    }

    String getTargetDeployNamespace() {
        return targetDeployNamespace
    }

    AppServicePublisher setTargetDeployNamespace(String targetDeployNamespace) {
        this.targetDeployNamespace = targetDeployNamespace
        return this
    }

    DeployCluster getTargetDeployCluster() {
        return targetDeployCluster
    }

    AppServicePublisher setTargetDeployCluster(DeployCluster targetDeployCluster) {
        this.targetDeployCluster = targetDeployCluster
        return this
    }

    int getDefaultReplicas() {
        return defaultReplicas
    }

    AppServicePublisher setDefaultReplicas(int defaultReplicas) {
        this.defaultReplicas = defaultReplicas
        return this
    }

    private void markPublish(boolean needPublish) {
        this.needPublish = needPublish
    }

    private static void checkNotNull(String varName, Object varObj) {
        if (varObj == null) {
            throw new IllegalArgumentException("${varName} should not be null")
        }
    }

    void publish(BuildNotifyCmd finishNotifyCmd) {
        checkNotNull("itemConfig", this.itemConfig)
        checkNotNull("targetImage", this.targetImage)
        checkNotNull("targetDeployEnv", this.targetDeployEnv)
        checkNotNull("targetDeployNamespace", this.targetDeployNamespace)
        checkNotNull("targetDeployCluster", this.targetDeployCluster)

        if (this.targetImage.isEmpty()) {
            return
        }

        saveOriginImage()
        if (!this.needPublish) {
            return
        }

        ensurePublish()
        if (!this.needPublish) {
            return
        }

        finishNotifyCmd.notifyMsg["${this.itemConfig.projectName}原始版本"] = this.originImage
        finishNotifyCmd.notifyMsg["${this.itemConfig.projectName}目标版本"] = this.targetImage

        deployProject()
        if (verifyProject()) {
            finishNotifyCmd.notifyMsg["${this.itemConfig.projectName}发布结果"] = "成功！！！"
            return
        }

        rollbackProject()
        finishNotifyCmd.notifyMsg["${this.itemConfig.projectName}发布结果"] = "失败，已回滚版本！！！"
        this.context.jenkins.error "发布${this.itemConfig.projectName}失败"
    }

    private void saveOriginImage() {
        this.context.jenkins.stage("项目${this.itemConfig.projectName}更新检测") {
            this.originImage = this.baseProcess.getDeployImage(this.itemConfig, this.targetDeployEnv)

            this.context.jenkins.echo "publishProject originImage=${this.originImage}, targetImage=${this.targetImage}"
            if (this.originImage == this.targetImage) {
                this.context.jenkins.echo "镜像版本一致，无须发布！！！"
                this.markPublish(false)
            }
        }
    }

    private void ensurePublish() {
        this.context.jenkins.stage("发布${itemConfig.projectName}确认") {
            def resultInput = new InputMessageCmd(this.context, this.baseProcess)
                    .setNotifyMsg("[IDC]确认发布服务${this.itemConfig.projectName}，当前镜像为${this.originImage}, 目标镜像为${this.targetImage}")
                    .addParameter(this.context.jenkins.choice(
                            name: "ignoreFlag", description: "跳过发布", choices: "NO\nYES"))
                    .addParameter(this.context.jenkins.choice(
                            name: "projectName", description: "项目名", choices: "${this.itemConfig.projectName}"))
                    .setOkBtnText("确认")
                    .addNotifyUserName(this.context.globalConfig.teamLeader)
                    .execute()
                    .getResult()
            if (resultInput["ignoreFlag"] == "YES") {
                this.markPublish(false)
            }
        }
    }

    private void deployProject() {
        this.context.jenkins.stage("部署运行${itemConfig.projectName}") {
            this.baseProcess.deployAppProjectYaml(this.itemConfig
                    , this.targetImage
                    , this.targetDeployEnv
                    , this.targetDeployNamespace
                    , this.defaultReplicas)

            this.baseProcess.checkAppProjectRunningSuccess(this.itemConfig
                    , this.targetImage
                    , this.targetDeployCluster
                    , this.targetDeployNamespace)
        }
    }

    private boolean verifyProject() {
        boolean verifySuccess = false
        this.context.jenkins.stage("发布验证") {
            InputMessageCmd messageCmd = new InputMessageCmd(context, this.baseProcess)
                    .setNotifyMsg("[${this.targetDeployEnv}]服务${this.itemConfig.projectName}已更新镜像至${this.targetImage}, 请验证")
                    .setNotifyToGlobal(true)
                    .addNotifyUserName(this.context.globalConfig.teamLeader)
                    .addParameter(this.context.jenkins.choice(
                            name: "TestResultChoice", description: "测试结果", choices: "NO\nYES"))
                    .setOkBtnText("确认")
            messageCmd.execute()

            def inputResult = messageCmd.getResult()
            if (inputResult["TestResultChoice"] == "YES") {
                verifySuccess = true
            } else {
                this.itemConfig.needRollback = true
            }
        }
        return verifySuccess
    }

    private void rollbackProject() {
        this.context.jenkins.stage("执行回滚") {
            this.baseProcess.rollbackDeployImage(this.itemConfig
                    , this.originImage
                    , this.targetDeployEnv)
            this.baseProcess.checkAppProjectRunningSuccess(this.itemConfig
                    , this.originImage
                    , this.targetDeployCluster
                    , this.targetDeployNamespace
                    , true)
        }
    }
}
