package pipeline.athena

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

/**
 *  开发管理平台流程
 */
class DevWebadminProcess extends BaseProcess {

    DevWebadminProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [AthenaProjectNames.WEBADMIN_SERVER
                , AthenaProjectNames.MANAGE_WEBAPP
                , AthenaProjectNames.WEBADMIN_GATEWAY
                , AthenaProjectNames.SWAGGER_WEBAPP
                , AthenaProjectNames.SWAGGER_PROXY]
    }

    private static DeployCluster getDeployCluster() {
        return DeployCluster.UG_PUBLIC
    }

    private static String getVirtualEnv() {
//        return "athena-dev"
        return null
    }

    private static String getDeployArgoEnv() {
//        return "public-athena-dev"
        return "public"
    }

    private static String getK8sNamespace() {
//        return "athena-dev"
        return "athena"
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig webadminGatewayConfig = this.context.projectsConfig.get(AthenaProjectNames.WEBADMIN_GATEWAY)
        this.finishedNotifyCmd.addToNotifyUserList(webadminGatewayConfig.projectMaintainer)
        String targetWebadminGatewayImage = ""

        ProjectItemConfig webadminServerConfig = this.context.projectsConfig.get(AthenaProjectNames.WEBADMIN_SERVER)
        this.finishedNotifyCmd.addToNotifyUserList(webadminServerConfig.projectMaintainer)
        String targetWebadminServerImage = ""

        ProjectItemConfig manageWebappConfig = this.context.projectsConfig.get(AthenaProjectNames.MANAGE_WEBAPP)
        this.finishedNotifyCmd.addToNotifyUserList(manageWebappConfig.projectMaintainer)
        String targetManageWebappImage = ""

        ProjectItemConfig swaggerProxyConfig = this.context.projectsConfig.get(AthenaProjectNames.SWAGGER_PROXY)
        this.finishedNotifyCmd.addToNotifyUserList(swaggerProxyConfig.projectMaintainer)
        String targetSwaggerProxyImage = ""

        ProjectItemConfig swaggerWebappConfig = this.context.projectsConfig.get(AthenaProjectNames.SWAGGER_WEBAPP)
        this.finishedNotifyCmd.addToNotifyUserList(swaggerWebappConfig.projectMaintainer)
        String targetSwaggerWebappImage = ""

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(webadminGatewayConfig, getDeployCluster(), getVirtualEnv())
            this.projectConfigCheck(webadminServerConfig, getDeployCluster(), getVirtualEnv())
            this.projectConfigCheck(manageWebappConfig, getDeployCluster(), getVirtualEnv())
            this.projectConfigCheck(swaggerProxyConfig, getDeployCluster(), getVirtualEnv())
            this.projectConfigCheck(swaggerWebappConfig, getDeployCluster(), getVirtualEnv())
        }

        this.context.jenkins.stage("代码扫描") {
            this.sonarCheck(webadminGatewayConfig)
            this.sonarCheck(webadminServerConfig)
            this.sonarCheck(manageWebappConfig)
            this.sonarCheck(swaggerProxyConfig)
            this.sonarCheck(swaggerWebappConfig)

        }

        this.context.jenkins.stage("编译镜像集") {
            targetWebadminGatewayImage = this.buildAppProject(webadminGatewayConfig)
            targetWebadminServerImage = this.buildAppProject(webadminServerConfig)
            targetManageWebappImage = this.buildAppProject(manageWebappConfig)
            targetSwaggerProxyImage = this.buildAppProject(swaggerProxyConfig)
            targetSwaggerWebappImage = this.buildAppProject(swaggerWebappConfig)
        }

        this.context.jenkins.stage("部署镜像集") {
            this.deployAppProjectYaml(webadminGatewayConfig
                    , targetWebadminGatewayImage
                    , getDeployArgoEnv()
                    , getK8sNamespace())

            this.deployAppProjectYaml(webadminServerConfig
                    , targetWebadminServerImage
                    , getDeployArgoEnv()
                    , getK8sNamespace())

            this.deployAppProjectYaml(manageWebappConfig
                    , targetManageWebappImage
                    , getDeployArgoEnv()
                    , getK8sNamespace())

            this.deployAppProjectYaml(swaggerProxyConfig
                    , targetSwaggerProxyImage
                    , getDeployArgoEnv()
                    , getK8sNamespace())

            this.deployAppProjectYaml(swaggerWebappConfig
                    , targetSwaggerWebappImage
                    , getDeployArgoEnv()
                    , getK8sNamespace())
        }

        this.context.jenkins.stage("检测运行") {
            this.checkAppProjectRunningSuccess(webadminGatewayConfig
                    , targetWebadminGatewayImage
                    , getDeployCluster()
                    , getK8sNamespace())

            this.checkAppProjectRunningSuccess(webadminServerConfig
                    , targetWebadminServerImage
                    , getDeployCluster()
                    , getK8sNamespace())

            this.checkAppProjectRunningSuccess(manageWebappConfig
                    , targetManageWebappImage
                    , getDeployCluster()
                    , getK8sNamespace())

            this.checkAppProjectRunningSuccess(swaggerProxyConfig
                    , targetSwaggerProxyImage
                    , getDeployCluster()
                    , getK8sNamespace())

            this.checkAppProjectRunningSuccess(swaggerWebappConfig
                    , targetSwaggerWebappImage
                    , getDeployCluster()
                    , getK8sNamespace())
        }
    }
}
