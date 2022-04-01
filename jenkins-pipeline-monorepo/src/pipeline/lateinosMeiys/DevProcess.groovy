package pipeline.lateinosMeiys

import cmd.ArgoDeployCmd
import cmd.BuildAppImageCmd
import cmd.CheckAppReadinessCmd
import entity.AppStatus
import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

class DevProcess extends BaseProcess {

    protected DevProcess(Context context) {
        super(context)
    }

    @Override
    def careProjects() {
        return ["lateinos-controller-agent", "lateinos-dotnet-demo", "lateinos-java-demo"]
    }

    @Override
    void execute() {

        // 强制编译检测
        checkAllProjectBuildStage()

        // ------------------------------------- deploy测试 -------------------------------------

        /*
            逐个编译项目
            不能用for循环会导致CPS转换错误
         */

        def projectsConfig = this.context.projectsConfig

        String projectName = "lateinos-controller-agent"
        /*this.context.jenkins.stage(projectName) {
            this.buildApp(this.context, projectName, projectsConfig.get(projectName))
        }*/
        projectName = "lateinos-dotnet-demo"
        this.context.jenkins.stage(projectName) {
            this.buildApp(this.context, projectName, projectsConfig.get(projectName))
        }
        projectName = "lateinos-java-demo"
        this.context.jenkins.stage(projectName) {
            this.buildApp(this.context, projectName, projectsConfig.get(projectName))
        }

        /*String image = new EnvDeployImageCmd(this.context)
                .setProjectName(projectName)
                .setDeliveryGroup(this.context.globalConfig.deliveryGroup)
                .setDeployYamlFileName(projectsConfig.get(projectName).deployYamlFileName)
                .setArgocdGitUrl(this.context.globalConfig.defaultArgocdGitUrl)
                .setDeployEnv("dev")
                .execute().getResult()*/
    }

    def buildApp(Context context, String projectName, ProjectItemConfig config) {
        if (!config.needBuild) {
            return
        }

        this.context.jenkins.echo "当前构建: ${projectName}"
        config.status = "ing"

        String buildDir = config.buildDir
        this.context.jenkins.echo "构建目录: buildDir=${buildDir}"

        /*
            配置中心检查
        */

        // 检查配置项
        /*ConfigValidateResult configValidateResult = new ConfigValidateCmd(context)
                .setClusterName(DeployCluster.UG_DEV.getCode())
                .setProjectName(projectName)
                .setVirtualEnv("meiys-dev")
                .setGlobalConfig(config.globalConfig)
                .setVirtualEnvConfig(config.virtualEnvConfig)
                .setCmdbConfig(config.cmdbConfig)
                .execute().getResult()
        if (!configValidateResult.allSuccessed) {
            context.jenkins.error """配置中心检查失败: 
                                    allSuccessed=${configValidateResult.allSuccessed}
                                    failedItems=${configValidateResult.formatFailed()}"""
        }
        context.jenkins.echo "配置中心检查完成"*/

        /*
            中间件配置检查
        */

        /*
        // route检查
        BaseValidateResult routeValidateResult = new RouteValidateCmd(context)
                .setClusterName(DeployCluster.UG_DEV.getCode())
                .setConfig(config.route)
                .execute().getResult()
        if (!routeValidateResult.allSuccessed) {
            context.jenkins.error """中间件-路由配置检查失败:
                                    allSuccessed=${routeValidateResult.allSuccessed}
                                    failedItems=${routeValidateResult.formatFailed()}"""
        }
        context.jenkins.echo "中间件-路由配置检查完成"

        // idMaker检查
        BaseValidateResult idMakerValidateResult = new IdMakerValidateCmd(context)
                .setClusterName(DeployCluster.UG_DEV.getCode())
                .setConfig(config.idMaker)
                .execute().getResult()
        if (!idMakerValidateResult.allSuccessed) {
            context.jenkins.error """中间件-IdMaker配置检查失败:
                                    allSuccessed=${idMakerValidateResult.allSuccessed}
                                    failedItems=${idMakerValidateResult.formatFailed()}"""
        }
        context.jenkins.echo "中间件-IdMaker配置检查完成"

        // storage检查
        BaseValidateResult storageValidateResult = new StorageValidateCmd(context)
                .setClusterName(DeployCluster.UG_DEV.getCode())
                .setConfig(config.storage)
                .execute().getResult()
        if (!storageValidateResult.allSuccessed) {
            context.jenkins.error """中间件-存储配置检查失败:
                                    allSuccessed=${storageValidateResult.allSuccessed}
                                    failedItems=${storageValidateResult.formatFailed()}"""
        }
        context.jenkins.echo "中间件-存储配置检查完成"

        // RocketMq检查
        BaseValidateResult rocketMqValidateResult = new RocketMqValidateCmd(context)
                .setClusterName(DeployCluster.UG_DEV.getCode())
                .setConfig(config.rocketMq)
                .execute().getResult()
        if (!rocketMqValidateResult.allSuccessed) {
            context.jenkins.error """中间件-RocketMq配置检查失败:
                                    allSuccessed=${rocketMqValidateResult.allSuccessed}
                                    failedItems=${rocketMqValidateResult.formatFailed()}"""
        }
        context.jenkins.echo "中间件-RocketMq配置检查完成"

        // 日志采集检查
        BaseValidateResult logcollectorValidateResult = new LogcollectorValidateCmd(context)
                .setClusterName(DeployCluster.UG_DEV.getCode())
                .setConfig(config.logcollector)
                .execute().getResult()
        if (!logcollectorValidateResult.allSuccessed) {
            context.jenkins.error """中间件-日志采集配置检查失败:
                                    allSuccessed=${logcollectorValidateResult.allSuccessed}
                                    failedItems=${logcollectorValidateResult.formatFailed()}"""
        }
        context.jenkins.echo "中间件-日志采集配置检查完成"

        // redis检查
        BaseValidateResult redisValidateResult = new RedisValidateCmd(context)
                .setClusterName(DeployCluster.UG_DEV.getCode())
                .setConfig(config.redis)
                .execute().getResult()
        if (!redisValidateResult.allSuccessed) {
            context.jenkins.error """中间件-Redis配置检查失败:
                                    allSuccessed=${redisValidateResult.allSuccessed}
                                    failedItems=${redisValidateResult.formatFailed()}"""
        }
        context.jenkins.echo "中间件-Redis配置检查完成"

        // 分布式锁检查
        BaseValidateResult distributedLockValidateResult = new DistributedLockValidateCmd(context)
                .setClusterName(DeployCluster.UG_DEV.getCode())
                .setConfig(config.distributedLock)
                .execute().getResult()
        if (!distributedLockValidateResult.allSuccessed) {
            context.jenkins.error """中间件-分布式锁配置检查失败:
                                    allSuccessed=${distributedLockValidateResult.allSuccessed}
                                    failedItems=${distributedLockValidateResult.formatFailed()}"""
        }
        context.jenkins.echo "中间件-分布式锁配置检查完成"

        // 分布式锁检查
        BaseValidateResult asyncTaskValidateResult = new AsyncTaskValidateCmd(context)
                .setClusterName(DeployCluster.UG_DEV.getCode())
                .setConfig(config.asyncTask)
                .execute().getResult()
        if (!asyncTaskValidateResult.allSuccessed) {
            context.jenkins.error """中间件-异步任务调度配置检查失败:
                                    allSuccessed=${asyncTaskValidateResult.allSuccessed}
                                    failedItems=${asyncTaskValidateResult.formatFailed()}"""
        }
        context.jenkins.echo "中间件-异步任务调度配置检查完成"
*/

        /*
            打包发布
        */

        // 制作应用镜像
        String image = new BuildAppImageCmd(context)
                .setCodeType(config.codeType)
                .setBuildDir(buildDir)
                .setProjectName(projectName)
                .setBaseImage(config.buildBaseImage)
                .setRegistry(this.context.globalConfig.defaultRegistry)
                .setImagePath(this.context.globalConfig.defaultImagePath)
                .execute().getResult()

        // 部署服务
        new ArgoDeployCmd(context)
                .setProjectName(projectName)
                .setDeployImageName(config.deployImageName)
                .setDeliveryGroup(this.context.globalConfig.deliveryGroup)
                .setArgocdGitUrl(this.context.globalConfig.defaultArgocdGitUrl)
                .setDeployYamlFileName(config.deployYamlFileName)
                .setTargetImage(image)
                .setDeployEnv("public")
                .setCodeType(config.codeType)
                .setDeployBaseTemplate(config.deployBaseTemplate)
                .setDeployNamespace(this.context.globalConfig.deliveryGroup)
                .execute()
        this.context.jenkins.echo "提交部署yaml"

        // 探测部署状态
        AppStatus appStatus = new CheckAppReadinessCmd(this.context)
                .setProjectName(projectName)
                .setCluster(DeployCluster.UG_PUBLIC.getCode())
                .setNamespace(this.context.globalConfig.deliveryGroup)
                .setTargetImage(image)
                .setTimeoutSecond(600)  // 5min超时
                .execute().getResult()
        if (!appStatus.isReadiness) {
            this.context.jenkins.error "App部署失败"
        }
    }
}
