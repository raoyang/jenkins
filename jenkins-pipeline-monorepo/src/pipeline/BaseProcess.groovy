package pipeline

import cmd.*
import cmd.middleware.*
import entity.*
import entity.config.ConfigValidateResult
import entity.middleware.BaseValidateResult
import enums.DeployCluster

/**
 *  流程抽象的基类, 可以作为实现的基础参考
 */
abstract class BaseProcess {
    protected Context context

    protected LatestCommitMsg latestCommitMsg
    protected BuildNotifyCmd finishedNotifyCmd

    protected boolean processFailed

    protected BaseProcess(Context context) {
        this.context = context

        this.processFailed = false
    }

    /**
     *  声明项目关心的工程列表
     * @return
     */
    protected abstract def careProjects()

    protected abstract void execute()

    /**
     *  通用入口流程
     */
    BaseProcess handleProcess() {
        finishedNotifyCmd = constructNotify()
        try {
            new InitGitCmd(this.context).execute()

            latestCommitMsg = new GetLatestCommitMsg(this.context).execute().getResult()

            this.context.jenkins.echo "最近提交信息，${latestCommitMsg.toString()}"

            // 增加最近提交信息
            finishedNotifyCmd.notifyMsg["提交短Hash"] = latestCommitMsg.shortCommitHash
            finishedNotifyCmd.notifyMsg["提交信息"] = latestCommitMsg.subject
            finishedNotifyCmd.notifyMsg["最近提交者"] = latestCommitMsg.committerName

            // 强制编译的检测环节
            checkAllProjectBuildStage()

            // 执行具体子分支流程
            execute()

        } catch (e) {
            this.processFailed = true
            finishedNotifyCmd.setErrorNotify(true)
            finishedNotifyCmd.notifyMsg["失败原因"] = e.getMessage()

            throw e
        } finally {
            if (finishedNotifyCmd.notifyUserList.isEmpty()) {
                finishedNotifyCmd.addToNotifyUserList(this.context.globalConfig.teamLeader)
            }

            this.context.jenkins.stage("通知") {
                finishedNotifyCmd.execute()
            }
        }
        return this
    }

    protected ProjectItemConfig getItemConfig(String projectName) {
        return this.context.projectsConfig.get(projectName)
    }


    // sonar代码扫描
    protected void sonarCheck(ProjectItemConfig itemConfig) {
        if (!itemConfig.needBuild) {
            return
        }

        SonarCheckMsg checkMsg = new SonarCheckCmd(this.context)
                .setProjectItemConfig(itemConfig)
                .setGitCommitter(this.latestCommitMsg.committerName)
                .execute()
                .getResult()
        this.context.jenkins.echo "${checkMsg.toString()}"
        finishedNotifyCmd.buttonUrl["扫描${itemConfig.projectName}结果"] = checkMsg.sonarUrl

        if (!checkMsg.success) {
            this.context.jenkins.error checkMsg.errorMsg
        }
    }

    // 项目配置检测
    protected void projectConfigCheck(ProjectItemConfig itemConfig
                                      , DeployCluster cluster
                                      , String virtualEnv = null) {
        if (!itemConfig.needBuild) {
            return
        }

        InputMessageCmd messageCmd = this.initInputMessageCmd(itemConfig.projectMaintainer)

        Exception pipelineIllegal = null
        this.context.jenkins.retry(Integer.MAX_VALUE) {
            ConfigValidateResult validateResult
            try {
                validateResult = new ConfigValidateCmd(this.context)
                        .setClusterName(cluster.getCode())
                        .setVirtualEnv(virtualEnv)
                        .setProjectName(itemConfig.projectName)
                        .setGlobalConfig(itemConfig.globalConfig)
                        .setVirtualEnvConfig(itemConfig.virtualEnvConfig)
                        .setCmdbConfig(itemConfig.cmdbConfig)
                        .execute()
                        .getResult()
            } catch (Exception e) {
                pipelineIllegal = e
                return
            }

            if (!validateResult.allSuccessed) {
                messageCmd.setNotifyMsg("配置检测失败, 异常条目详情:\n ${validateResult.formatFailed()}")
                messageCmd.execute()

                this.context.jenkins.error "触发配置重新检测..."
            }
        }
        if (pipelineIllegal != null) {
            throw pipelineIllegal
        }

        middlewareCheck(itemConfig, cluster, virtualEnv)
    }

    protected InputMessageCmd initInputMessageCmd(String projectMaintainer){
        InputMessageCmd messageCmd = new InputMessageCmd(this.context, this)
        messageCmd.okBtnText = "已处理"

        messageCmd.addNotifyUserName(this.context.globalConfig.teamLeader)
        messageCmd.addSubmitter(this.context.globalConfig.teamLeader)
        messageCmd.addNotifyUserName(projectMaintainer)
        messageCmd.addSubmitter(projectMaintainer)

        return messageCmd
    }

    // 中间件整体检查
    protected void middlewareCheck(ProjectItemConfig itemConfig
                                   , DeployCluster cluster
                                   , String virtualEnv = null) {
        if (!itemConfig.needBuild) {
            return
        }

        def cmd
        if (itemConfig.route != null) {
            cmd = new RouteValidateCmd(this.context)
            this.middlewareSingleCheck(itemConfig, cmd, "中间件-路由配置", itemConfig.route, cluster, virtualEnv)
        }
        if (itemConfig.idMaker != null) {
            cmd = new IdMakerValidateCmd(this.context)
            this.middlewareSingleCheck(itemConfig, cmd, "中间件-ID生成器配置", itemConfig.idMaker, cluster, virtualEnv)
        }
        if (itemConfig.storage != null) {
            cmd = new StorageValidateCmd(this.context)
            this.middlewareSingleCheck(itemConfig, cmd, "中间件-文件存储配置", itemConfig.storage, cluster, virtualEnv)
        }
        if (itemConfig.rocketMq != null) {
            cmd = new RocketMqValidateCmd(this.context)
            this.middlewareSingleCheck(itemConfig, cmd, "中间件-RocketMQ配置", itemConfig.rocketMq, cluster, virtualEnv)
        }
        if (itemConfig.logcollector != null) {
            cmd = new LogcollectorValidateCmd(this.context)
            this.middlewareSingleCheck(itemConfig, cmd, "中间件-日志采集配置", itemConfig.logcollector, cluster, virtualEnv)
        }
        if (itemConfig.redis != null) {
            cmd = new RedisValidateCmd(this.context)
            this.middlewareSingleCheck(itemConfig, cmd, "中间件-Redis配置", itemConfig.redis, cluster, virtualEnv)
        }
        if (itemConfig.distributedLock != null) {
            cmd = new DistributedLockValidateCmd(this.context)
            this.middlewareSingleCheck(itemConfig, cmd, "中间件-分布式锁配置", itemConfig.distributedLock, cluster, virtualEnv)
        }
        if (itemConfig.asyncTask != null) {
            cmd = new AsyncTaskValidateCmd(this.context)
            this.middlewareSingleCheck(itemConfig, cmd, "中间件-异步任务调度配置", itemConfig.asyncTask, cluster, virtualEnv)
        }
    }

    // 中间件单项检查
    protected  <T extends AbstractMiddlewareValidateCmd> void middlewareSingleCheck(ProjectItemConfig itemConfig
                                                              , T cmd
                                                              , String describtion
                                                              , def middlewareConfig
                                                              , DeployCluster cluster
                                                              , String virtualEnv = null) {
        if (!middlewareConfig) {
            return
        }

        InputMessageCmd messageCmd = this.initInputMessageCmd(itemConfig.projectMaintainer)

        Exception pipelineIllegal = null
        this.context.jenkins.retry(Integer.MAX_VALUE) {
            BaseValidateResult validateResult
            try {
                 validateResult = cmd
                        .setClusterName(cluster.getCode())
                        .setVirtualEnv(virtualEnv)
                        .setConfig(middlewareConfig)
                        .execute()
                        .getResult()
            } catch (Exception e) {
                pipelineIllegal = e
                return
            }

            if (!validateResult.allSuccessed) {
                messageCmd.setNotifyMsg("${describtion}检测失败, 异常条目详情:\n ${validateResult.formatFailed()}")
                messageCmd.execute()

                this.context.jenkins.error "触发${describtion}重新检测..."
            }
        }
        if (pipelineIllegal != null) {
            throw pipelineIllegal
        }
    }

    /**
     *  获取项目特定环境的部署镜像版本
     * @param itemConfig
     * @param deployEnv
     * @return
     */
    protected String getDeployImage(ProjectItemConfig itemConfig, String deployEnv) {
        if (!itemConfig.needBuild) {
            return ""
        }

        String image = new EnvDeployImageCmd(this.context)
                .setProjectName(itemConfig.projectName)
                .setDeployImageName(itemConfig.deployImageName)
                .setArgocdGitUrl(this.context.globalConfig.defaultArgocdGitUrl)
                .setDeliveryGroup(this.context.globalConfig.deliveryGroup)
                .setDeployYamlFileName(itemConfig.deployYamlFileName)
                .setDeployEnv(deployEnv)
                .execute()
                .getResult()
        this.context.jenkins.echo "project=${itemConfig.projectName}'s deploy image is ${image}"
        return image
    }

    /**
     * 回滚特定的部署项目
     */
    void rollbackDeployImage(ProjectItemConfig itemConfig
            , String originImage
            , String deployEnv) {
        if (!itemConfig.needRollback) {
            return
        }

        this.context.jenkins.echo "rollback project=${itemConfig.projectName}'s deploy image to ${originImage}, deployEnv=${deployEnv}"
        if (originImage.isEmpty()) {
            return
        }

        this.deployAppProjectYaml(itemConfig
                , originImage
                , deployEnv
                , "not-need"
                , 1
                , "rollback")
    }

    /**
     *  强制编译检测的确认环节
     */
    protected void checkAllProjectBuildStage() {
        this.context.jenkins.stage("强制编译检测") {
            if (!this.context.jenkins.params.isBuildAll) {
                return
            }

            def careProjectNames = careProjects()
            if (careProjectNames == null) {
                return
            }

            InputMessageCmd messageCmd = new InputMessageCmd(this.context, this)
                    .setOkBtnText("确认")
                    .setNotifyMsg("启动强制编译，请确认需要强制重编的项目名称")
                    .addParameter(this.context.jenkins.choice(
                            name: "forceBuild",
                            choices: "YES"))
                    .addParameter(this.context.jenkins.choice(
                            name: "branchName",
                            choices: "${this.context.branchName}"))

            for (String projectName : careProjectNames) {
                ProjectItemConfig itemConfig = this.context.projectsConfig.get(projectName)
                messageCmd.addParameter(this.context.jenkins.booleanParam(
                        name: "build-${itemConfig.projectName}",
                        description: "强制编译${itemConfig.projectName}",
                        defaultValue: false
                ))
            }

            String projectAndDependencies = ""
            def inputResult = messageCmd.execute().getResult()
            for (String projectName : careProjectNames) {
                ProjectItemConfig itemConfig = this.context.projectsConfig.get(projectName)
                if (!inputResult["build-${itemConfig.projectName}"]) {
                    itemConfig.needBuild = false
                }
            }

            // 同时标记依赖项目
            for (String projectName : careProjectNames) {
                ProjectItemConfig itemConfig = this.context.projectsConfig.get(projectName)
                if (inputResult["build-${itemConfig.projectName}"]) {
                    projectAndDependencies += "勾选: " + projectName + "\n"
                    if (itemConfig.allBeDependencies) {
                        for (String dependence : itemConfig.allBeDependencies) {
                            projectAndDependencies += "关联: - " + dependence + "\n"
                            ProjectItemConfig dependenceProject = this.context.projectsConfig.get(dependence)
                            dependenceProject.needBuild = true
                        }
                    }
                }
            }

            // 打印构建树
            this.context.jenkins.echo "强制编译需要构建的项目：\n${projectAndDependencies}"
        }
    }

    /**
     * 编译一个应用镜像
     * @param itemConfig
     * @return 编译的Docker的镜像地址
     */
    protected String buildAppProject(ProjectItemConfig itemConfig) {
        if (!itemConfig.needBuild) {
            return null
        }

        String image = new BuildAppImageCmd(context)
                .setCodeType(itemConfig.codeType)
                .setBuildDir(itemConfig.buildDir)
                .setProjectName(itemConfig.projectName)
                .setRegistry(context.globalConfig.defaultRegistry)
                .setImagePath(context.globalConfig.defaultImagePath)
                .setBaseImage(itemConfig.buildBaseImage)
                .setPackageTool(itemConfig.packageTool)
                .setTargetStaticDir(itemConfig.targetStaticDir)
                .execute()
                .getResult()

        this.finishedNotifyCmd.notifyMsg["${itemConfig.projectName}镜像"] = image

        return image
    }

    /**
     *  部署工程脚本
     */
    void deployAppProjectYaml(ProjectItemConfig itemConfig
                                        , String itemImage
                                        , String deployArgoEnv
                                        , String deployNamespace
                                        , int defaultReplicas = 1
                                        , String deployOption = "ci") {
        if (!itemConfig.needBuild) {
            return
        }

        ArgoDeployCmd deployCmd = new ArgoDeployCmd(context)
                .setProjectName(itemConfig.projectName)
                .setDeployImageName(itemConfig.deployImageName)
                .setVariables(itemConfig.deployVariables)
                .setDeliveryGroup(context.globalConfig.deliveryGroup)
                .setArgocdGitUrl(context.globalConfig.defaultArgocdGitUrl)
                .setDeployYamlFileName(itemConfig.deployYamlFileName)
                .setDeployEnv(deployArgoEnv)
                .setTargetImage(itemImage)
                .setCodeType(itemConfig.codeType)
                .setDeployBaseTemplate(itemConfig.deployBaseTemplate)
                .setDeployReplicas("${defaultReplicas}")
                .setDeployNamespace(deployNamespace)
                .setOption(deployOption)
        /**
         *  项目配置可以覆盖全局配置
         */
        if (itemConfig.deployDeliveryGroup != null && !itemConfig.deployDeliveryGroup.isEmpty()) {
            deployCmd.setDeliveryGroup(itemConfig.deployDeliveryGroup)
        }
        if (itemConfig.argocdGitUrl != null && !itemConfig.argocdGitUrl.isEmpty()) {
            deployCmd.setArgocdGitUrl(itemConfig.argocdGitUrl)
        }
        deployCmd.execute()
    }

    /**
     * 检测一个工程部署是否成功
     * @param itemConfig
     * @param itemImage 镜像地址
     * @param deployArgoEnv argo上声明的环境目录（dev, test等）
     * @param deployCluster 部署的集群， 参见DeployCluster
     * @param deployNamespace 部署到集群的命名空间
     */
    void checkAppProjectRunningSuccess(ProjectItemConfig itemConfig
                                                 , String itemImage
                                                 , DeployCluster deployCluster
                                                 , String deployNamespace
                                                 , boolean isRollback = false) {
        if (!itemConfig.needBuild) {
            return
        }

        if (isRollback) {
            if (itemImage.isEmpty()) {
                return
            }

            if (!itemConfig.needRollback) {
                return
            }
        }

        /**
         *  不停重试，直到部署成功或者放弃
         */
        this.context.jenkins.retry(Integer.MAX_VALUE) {
            AppStatus appStatus = new CheckAppReadinessCmd(this.context)
                    .setProjectName(itemConfig.projectName)
                    .setCluster(deployCluster.getCode())
                    .setNamespace(deployNamespace)
                    .setTargetImage(itemImage)
                    .execute()
                    .getResult()
            if (!appStatus.isReadiness) {
                new InputMessageCmd(this.context, this)
                        .addNotifyUserName(this.context.globalConfig.teamLeader)
                        .addNotifyUserName(itemConfig.projectMaintainer)
                        .setNotifyMsg("部署${itemConfig.projectName}至集群${deployCluster}的命名空间${deployNamespace}失败")
                        .execute()
                this.context.jenkins.error "触发检测"
            }
        }
    }

    /**
     * 构建一个带有基础信息的通知
     * @return
     */
    BuildNotifyCmd constructNotify() {
        BuildNotifyCmd notifyCmd = new BuildNotifyCmd(this.context)
                .setTitle("${this.context.getGlobalConfig().getDeliveryGroup()}流水线通知, 分支${this.context.getBranchName()}")
        Map tips = [
                "起始版本": "${this.context.fromVersion}",
                "构建版本": "${this.context.toVersion}",
                "构建号" : "${this.context.jenkins.env.BUILD_NUMBER}"
        ]
        notifyCmd.setNotifyMsg(tips)

        // 构建基本
        String gitBranchUrl = this.context.jenkins.env.GIT_URL
        // 去除末尾的.git
        if (gitBranchUrl.endsWith(".git")) {
            gitBranchUrl = gitBranchUrl.substring(0, gitBranchUrl.length() - 4)
        }
        Map btnUrls = [
                "查看任务"  : this.context.jenkins.env.BUILD_URL,
                "查看日志"  : "${this.context.jenkins.env.BUILD_URL}/console",
                "查看分支项目": "${gitBranchUrl}/-/tree/${this.context.branchName}",
                "查看合并请求": "${gitBranchUrl}/-/merge_requests"
        ]
        notifyCmd.setButtonUrl(btnUrls)

        notifyCmd.setNotifyUserList(new ArrayList<String>())
        return notifyCmd
    }

    /**
     * 检测是否需要构建
     * @return
     */
    boolean checkNeedBuild() {
        boolean needBuild = false
        this.context.jenkins.stage("检测是否需要执行") {

            def projectList = this.careProjects()
            if (projectList == null) {
                // 无Project声明，则关心所有
                needBuild = true
                return
            }

            for (String projectName : projectList) {
                if (this.context.projectsConfig.get(projectName).needBuild) {
                    needBuild = true
                    return
                }
            }

            if (!needBuild) {
                this.context.jenkins.echo "无须执行，忽略此次变更通知"
            }
        }
        return needBuild
    }

    void checkProcessFailed() {
        if (this.processFailed) {
            this.context.jenkins.error "流水线执行失败"
        }
    }

    /**
      * 人工验证环节
      * @return 人工验证成功返回true
      */
    boolean manualVerification(List<String> notifyUserList, boolean rollbackAllFlag = false) {
        if (notifyUserList == null || notifyUserList.isEmpty()) {
            throw new IllegalArgumentException("notifyUserList is null or empty, please set it")
        }

        InputMessageCmd messageCmd = null
        def careProjectNames = this.careProjects()
        if (careProjectNames == null) {
            careProjectNames = []
        }

        this.context.jenkins.stage("验证确认") {
            messageCmd = new InputMessageCmd(this.context, this)
                    .setNotifyMsg("环境已经准备完毕，请进行测试验证！！！")
                    .setOkBtnText("确定")
                    .setNotifyToGlobal(true)
                    .addParameter(this.context.jenkins.choice(
                            name: "TestResultChoice", description: "测试结果", choices: "NO\nYES"))
                    .addParameter(this.context.jenkins.text(
                            name: "TestResultDescription", description: "测试结果描述"))
            for (String userName : notifyUserList) {
                messageCmd.addSubmitter(userName)
                messageCmd.addNotifyUserName(userName)
            }

            if (!rollbackAllFlag) {
                for (String projectName : careProjectNames) {
                    ProjectItemConfig configItem = this.context.projectsConfig.get(projectName)
                    if (configItem.needBuild) {
                        messageCmd.addParameter(this.context.jenkins.booleanParam(
                                name: "rollback-${configItem.projectName}",
                                description: "回滚${configItem.projectName}",
                                defaultValue: false
                        ))
                    }
                }
            }

            messageCmd.execute()
        }

        def testInputResult = messageCmd.getResult()
        if (testInputResult["TestResultChoice"] == "YES") {
            return true
        }

        for (String projectName : careProjectNames) {
            ProjectItemConfig configItem = this.context.projectsConfig.get(projectName)
            if (configItem.needBuild) {
                if (rollbackAllFlag) {
                    configItem.needRollback = true
                } else {
                    if (testInputResult["rollback-${configItem.projectName}"]) {
                        configItem.needRollback = true
                    }
                }
            }
        }

        return false
    }

    /**
     * 合并至开发类型分支
     * @param sourceBranchName
     */
    void mergeToDev(String sourceBranchName) {
        List<String> branchNames = new GetAllBranchesCmd(this.context).execute().getResult()

        this.context.jenkins.echo "branchNames=${branchNames}"

        for (String branchName : branchNames) {
            if (!branchName.startsWith("dev-")) {
                continue
            }

            this.processMerge(sourceBranchName, branchName)
        }
    }

    /**
     *  执行分支合并，同时展示stage的名称
     * @param sourceBranchName
     * @param targetBranchName
     * @param stageName
     * @param markProcessFailed 是否标记整个流程失败
     * @return
     */
    boolean execMergeCmd(String sourceBranchName
                         , String targetBranchName
                         , String stageName
                         , boolean markProcessFailed = true) {
        // 容忍部分失败
        try {
            if (stageName != null) {
                this.context.jenkins.stage(stageName) {
                    new MergeCodeCmd(this.context)
                            .setSourceBranch(sourceBranchName)
                            .setTargetBranch(targetBranchName)
                            .execute()
                }
            } else {
                new MergeCodeCmd(this.context)
                        .setSourceBranch(sourceBranchName)
                        .setTargetBranch(targetBranchName)
                        .execute()
            }
            finishedNotifyCmd.notifyMsg["代码从${sourceBranchName}合并至${targetBranchName}"] = "成功"

            return true
        } catch (Exception e) {
            if (markProcessFailed) {
                finishedNotifyCmd.notifyMsg["代码从${sourceBranchName}合并至${targetBranchName}"] = "失败，${e.getMessage()}"
                this.processFailed = true
            }
        }
        return false
    }

    /**
     *  处理合并的步骤，不随意跳过
     */
    void processMerge(String sourceBranchName, String targetBranchName) {
        this.context.jenkins.stage("从${sourceBranchName}合并至${targetBranchName}") {
            boolean success = this.execMergeCmd(sourceBranchName
                    , targetBranchName
                    , null
                    , false)
            if (success) {
                return
            }

            InputMessageCmd messageCmd = new InputMessageCmd(this.context, this)
            messageCmd.setOkBtnText("已处理")
            messageCmd.setNotifyToGlobal(true)
            messageCmd.setNotifyMsg("代码合并操作(从${sourceBranchName}合并至${targetBranchName})失败, 请处理")
            messageCmd.addNotifyUserName(this.context.globalConfig.teamLeader)
            messageCmd.execute()
        }
    }
}
