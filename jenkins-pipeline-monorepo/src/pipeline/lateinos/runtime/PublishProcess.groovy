package pipeline.lateinos.runtime

import cmd.InputMessageCmd
import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.AppServicePublisher
import pipeline.BaseProcess
import pipeline.lateinos.LateinosProjectNames

/**
 *  正式发布流程
 */
class PublishProcess extends BaseProcess {
    PublishProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return null
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.setNotifyToGlobalFlag(true)

        ProjectItemConfig controllerAgentConfig = this.context.projectsConfig.get(LateinosProjectNames.CONTROLLER_AGENT)
        String targetControllerAgentImage = ""

        ProjectItemConfig apiGatewayConfig = this.context.projectsConfig.get(LateinosProjectNames.API_GATEWAY)
        String targetApiGatewayImage = ""

        ProjectItemConfig runtimeServerConfig = this.context.projectsConfig.get(LateinosProjectNames.RUNTIME_SERVER)
        String targetRuntimeServerImage = ""

        ProjectItemConfig apiServerConfig = this.context.projectsConfig.get(LateinosProjectNames.API_SERVER)
        String targetApiServerImage = ""

        // 首先确认快速获取预发布环境的镜像
        this.context.jenkins.stage("提取镜像") {
            targetControllerAgentImage = this.getDeployImage(controllerAgentConfig, "test")
            targetApiGatewayImage = this.getDeployImage(apiGatewayConfig, "test")
            targetRuntimeServerImage = this.getDeployImage(runtimeServerConfig, "test")
            targetApiServerImage = this.getDeployImage(apiServerConfig, "test")
        }

        // 配置检测
        this.context.jenkins.stage("配置检测") {
            this.projectConfigCheck(controllerAgentConfig, DeployCluster.UG_IDC)
            this.projectConfigCheck(apiGatewayConfig, DeployCluster.UG_IDC)
            this.projectConfigCheck(runtimeServerConfig, DeployCluster.UG_IDC)
            this.projectConfigCheck(apiServerConfig, DeployCluster.UG_IDC)
        }

        // 发布Controller Agent
        new AppServicePublisher(this.context, this)
                .setItemConfig(controllerAgentConfig)
                .setTargetImage(targetControllerAgentImage)
                .setDefaultReplicas(2)
                .setTargetDeployCluster(DeployCluster.UG_IDC)
                .setTargetDeployEnv("idc")
                .setTargetDeployNamespace("lateinos")
                .publish(this.finishedNotifyCmd)

        // 发布API Gateway
        new AppServicePublisher(this.context, this)
                .setItemConfig(apiGatewayConfig)
                .setTargetImage(targetApiGatewayImage)
                .setDefaultReplicas(2)
                .setTargetDeployCluster(DeployCluster.UG_IDC)
                .setTargetDeployEnv("idc")
                .setTargetDeployNamespace("lateinos")
                .publish(this.finishedNotifyCmd)

        // 发布Runtime Server
        new AppServicePublisher(this.context, this)
                .setItemConfig(runtimeServerConfig)
                .setTargetImage(targetRuntimeServerImage)
                .setDefaultReplicas(2)
                .setTargetDeployCluster(DeployCluster.UG_IDC)
                .setTargetDeployEnv("idc")
                .setTargetDeployNamespace("lateinos")
                .publish(this.finishedNotifyCmd)

        // 发布API Server
        new AppServicePublisher(this.context, this)
                .setItemConfig(apiServerConfig)
                .setTargetImage(targetApiServerImage)
                .setDefaultReplicas(2)
                .setTargetDeployCluster(DeployCluster.UG_IDC)
                .setTargetDeployEnv("idc")
                .setTargetDeployNamespace("lateinos")
                .publish(this.finishedNotifyCmd)
    }
}
