package cmd

import entity.Context
import entity.GlobalConfig
import entity.ProjectItemConfig

/**
 * 上下文初始化命令
 */
class ContextInfoCmd extends AbstractCmd<Context> {

    private def jenkins
    private String filePath
    private boolean isBuildAll

    private String branchName

    // 自己指定版本范围
    private String fromVersion
    private String toVersion

    private Context resultImpl

    ContextInfoCmd(jenkins) {
        this.jenkins = jenkins
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("filePath", this.filePath)

        this.isBuildAll = this.jenkins.params.isBuildAll
        if (this.branchName == null) {
            this.branchName = this.jenkins.params.buildBranch
        }
        if (this.jenkins.params.publishForceBuildAll != null) {
            this.isBuildAll = true
        }

        Context context = new Context(jenkins)
        context.setBranchName(this.branchName)

        // 项目配置初始化
        def projectConfig = this.jenkins.libraryResource this.filePath
        def config = this.jenkins.readYaml text: projectConfig
        this.jenkins.echo "读取项目配置文件: config=${config}"
        this.parseConfig(context, config)

        // 源码变更文件
        if (this.toVersion == null) {
            this.toVersion = this.jenkins.sh(script: "git log origin/${branchName} --pretty=format:%H -1", returnStdout: true).trim()
        }
        if (this.fromVersion == null) {
            this.fromVersion = this.jenkins.env.GIT_PREVIOUS_SUCCESSFUL_COMMIT
        }
        context.setFromVersion(this.fromVersion)
        context.setToVersion(this.toVersion)

        this.jenkins.echo "代码变更版本: from=${this.fromVersion}, to=${this.toVersion}"

        // 首次全量
        List<String> fileList = null
        if (this.fromVersion == null) {
            this.isBuildAll = true
        } else {
            String forlderCmd = "git diff --name-only -r ${this.toVersion} ${this.fromVersion} | sort -u"
            String forlder = this.jenkins.sh(script: "${forlderCmd}", returnStdout: true).trim()
            fileList = forlder.split('\n')
            this.jenkins.echo "变更范围: fileList=${fileList}"
        }

        // 项目变更配置解析
        this.buildRuntimeInfo(context, fileList)

        // kubectl配置文件覆盖
        def exists = this.jenkins.fileExists "/root/.kube/tmp/config"
        if (exists) {
            this.jenkins.sh "cp -rf /root/.kube/tmp/config /root/.kube/config"
        }

        this.resultImpl = context
        return this
    }

    @Override
    Context getResult() {
        return this.resultImpl
    }

    /**
     * 解析配置
     *
     * @param context
     * @param config
     */
    private void parseConfig(Context context, def config) {
        GlobalConfig globalConfig = buildGlobalConfig(config)
        context.globalConfig = globalConfig

        Map<String, ProjectItemConfig> projectsConfig = this.buildProjectConfig(context.jenkins
                , context.globalConfig.deliveryGroup
                , config)
        context.projectsConfig = projectsConfig
    }

    /**
     * 构建全局项目配置
     *
     * @param context
     * @param config
     */
    private static GlobalConfig buildGlobalConfig(def config) {
        GlobalConfig globalConfig = new GlobalConfig()
        globalConfig.deliveryGroup = config.globalConfig.deliveryGroup
        globalConfig.defaultArgocdGitUrl = config.globalConfig.defaultArgocdGitUrl
        globalConfig.defaultRegistry = config.globalConfig.defaultRegistry
        globalConfig.defaultImagePath = config.globalConfig.defaultImagePath
        globalConfig.dingTalkAccessToken = config.globalConfig.dingTalkAccessToken
        globalConfig.dingTalkSecret = config.globalConfig.dingTalkSecret
        globalConfig.userPhones = config.globalConfig.userPhones
        globalConfig.userDetails = config.globalConfig.userDetails
        globalConfig.gitlabUrl = config.globalConfig.gitlabUrl
        globalConfig.gitlabProjectPath = config.globalConfig.gitlabProjectPath
        globalConfig.gitlabApiTokenId = config.globalConfig.gitlabApiTokenId
        globalConfig.gitlabJenkinsMultibranchHookBaseUrl = config.globalConfig.gitlabJenkinsMultibranchHookBaseUrl
        globalConfig.gitlabJenkinsMultibranchHookSecret = config.globalConfig.gitlabJenkinsMultibranchHookSecret
        globalConfig.teamLeader = config.globalConfig.teamLeader

        return globalConfig
    }

    /**
     * 构建项目配置
     * @param context
     * @param config
     */
    private Map<String, ProjectItemConfig> buildProjectConfig(jenkins, String deliveryGroup, def config) {
        def projects = config.projects
        Map<String, ProjectItemConfig> projectsConfig = new HashMap<>()
        for (def project : projects) {
            def itemConfig = jenkins.libraryResource "projectsConfig/${deliveryGroup}/${project}.yaml"
            def item = jenkins.readYaml text: itemConfig
            this.jenkins.echo "读取项目配置文件: ${project}=${item}"

            ProjectItemConfig projectItemConfig = new ProjectItemConfig()
            projectItemConfig.projectName = project.toString()
            projectItemConfig.deployImageName = item.deployImageName
            projectItemConfig.codeType = item.codeType
            projectItemConfig.codeDir = item.codeDir
            projectItemConfig.buildDir = item.buildDir
            projectItemConfig.buildType = item.buildType
            projectItemConfig.buildBaseImage = item.buildBaseImage
            projectItemConfig.packageTool = item.packageTool
            projectItemConfig.targetStaticDir = item.targetStaticDir
            projectItemConfig.deployDeliveryGroup = item.deployDeliveryGroup
            projectItemConfig.argocdGitUrl = item.argocdGitUrl
            projectItemConfig.deployYamlFileName = item.deployYamlFileName
            projectItemConfig.deployBaseTemplate = item.deployBaseTemplate
            projectItemConfig.rootSettings = item.rootSettings
            projectItemConfig.dependencies = item.dependencies
            projectItemConfig.projectMaintainer = item.projectMaintainer

            if (item.deployVariables != null) {
                if (item.deployVariables.size() > 0) {
                    Map deployVariables = [:]
                    for (def deployVariable in item.deployVariables) {
                        checkEmpty("deployVairables key for project=${project}", deployVariable["key"])
                        String deployKey = deployVariable["key"].toString()
                        checkEmpty("deployVariables value for project=${project}, key=${deployKey}", deployVariable["value"])
                        String deployValue = deployVariable["value"].toString()

                        deployVariables[deployKey] = deployValue
                    }
                    projectItemConfig.deployVariables = deployVariables
                }
            }

            // config检查配置
            if (item.globalConfig != null) {
                projectItemConfig.setGlobalConfig(item.globalConfig)
            }
            if (item.virtualEnvConfig != null) {
                projectItemConfig.setVirtualEnvConfig(item.virtualEnvConfig)
            }
            if (item.cmdbConfig != null) {
                projectItemConfig.setCmdbConfig(item.cmdbConfig)
            }

            // 中间件配置
            if (item.route != null) {
                projectItemConfig.setRoute(item.route)
            }
            if (item.idMaker != null) {
                projectItemConfig.setIdMaker(item.idMaker)
            }
            if (item.storage != null) {
                projectItemConfig.setStorage(item.storage)
            }
            if (item.rocketMq != null) {
                projectItemConfig.setRocketMq(item.rocketMq)
            }
            if (item.logcollector != null) {
                projectItemConfig.setLogcollector(item.logcollector)
            }
            if (item.redis != null) {
                projectItemConfig.setRedis(item.redis)
            }
            if (item.distributedLock != null) {
                projectItemConfig.setDistributedLock(item.distributedLock)
            }
            if (item.asyncTask != null) {
                projectItemConfig.setAsyncTask(item.asyncTask)
            }

            projectsConfig.put(project.toString(), projectItemConfig)
        }

        return projectsConfig
    }

    /**
     * 构建运行时初始化
     *
     * @param context
     * @param fileList
     */
    private void buildRuntimeInfo(Context context, List<String> fileList) {
        def beDepMap = this.buildAllBeDependencies(context.projectsConfig)

        for (def project : context.projectsConfig) {
            String str = beDepMap.get(project.key)
            if (str) {
                Set<String> beDepSet = new HashSet<>()
                beDepSet.addAll(str.split(","))
                project.value.allBeDependencies = beDepSet
            }
            this.jenkins.echo "所有被依赖项目: projectName=${project.key}, allBeDependencies=${str}}"

            // 全量更新
            if (this.isBuildAll) {
                project.value.needBuild = true
            }
            // 匹配提交记录
            else if (fileList != null) {
                String codeDir = project.value.codeDir
                def rootSettings = project.value.rootSettings
                for (String file : fileList) {
                    // TODO 过滤不必要的文件，比如README

                    // 本项目文件被更改
                    if (file.contains(codeDir)) {
                        project.value.needBuild = true
                        this.jenkins.echo "项目自身文件更改触发构建: project=${project.key}"
                    }
                    // 根配置被更改
                    else if (rootSettings) {
                        for (String setting : rootSettings) {
                            if (file.contains(setting)) {
                                project.value.needBuild = true
                                this.jenkins.echo "根配置更改触发构建: project=${project.key}"
                                break
                            }
                        }
                    }

                    // 依赖项被更改
                    if (project.value.needBuild && project.value.allBeDependencies) {
                        for (String dependency : project.value.allBeDependencies) {
                            this.checkDependencies(context, dependency, project.key)
                        }
                    }
                }
            }
        }
    }

    private Map<String, String> buildAllBeDependencies(Map<String, ProjectItemConfig> projectMap) {
        Map<String, String> beDepMap = new HashMap<>()
        for (def project : projectMap) {
            def dependencies = project.value.dependencies

            // 构建全被依赖项目
            if (dependencies) {
                for (String dependence : dependencies) {
                    String beDepStr = beDepMap.get(dependence)
                    if (!beDepStr) {
                        beDepStr = ""
                    }
                    if (beDepStr.size() > 0) {
                        beDepStr += ","
                    }
                    beDepStr += project.key
                    beDepMap.put(dependence, beDepStr)
                }
            }
        }

        return beDepMap
    }

    /**
     * 检查依赖项目是否发生变更
     * @param context
     * @param dependency
     * @param projectName
     * @return
     */
    private void checkDependencies(Context context, String dependency, String projectName) {
        ProjectItemConfig dependencyProject = context.projectsConfig.get(dependency)

        if (dependencyProject == null) {
            this.jenkins.error "依赖项目配置缺失: dependencyProject=${dependency}"
        }

        dependencyProject.needBuild = true
        this.jenkins.echo "依赖项被更改触发构建: project=${projectName}, dependency=${dependency}"
    }

    def getJenkins() {
        return jenkins
    }

    ContextInfoCmd setJenkins(jenkins) {
        this.jenkins = jenkins
        return this
    }

    String getFilePath() {
        return filePath
    }

    ContextInfoCmd setFilePath(String filePath) {
        this.filePath = filePath
        return this
    }


    String getFromVersion() {
        return fromVersion
    }

    ContextInfoCmd setFromVersion(String fromVersion) {
        this.fromVersion = fromVersion
        return this
    }

    String getToVersion() {
        return toVersion
    }

    ContextInfoCmd setToVersion(String toVersion) {
        this.toVersion = toVersion
        return this
    }

    ContextInfoCmd setBranchName(String branchName) {
        this.branchName = branchName
        return this
    }

    String getBranchName() {
        return this.branchName
    }
}
