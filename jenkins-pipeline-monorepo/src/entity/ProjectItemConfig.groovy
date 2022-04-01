package entity
/**
 * 单项目配置
 */
class ProjectItemConfig implements Serializable {

    private String projectName
    private String deployImageName
    private String codeType
    private String codeDir
    private String buildDir
    private String buildType
    private String buildBaseImage

    private String packageTool
    private String targetStaticDir
    private String deployDeliveryGroup
    private String argocdGitUrl
    private String deployYamlFileName
    private Map deployVariables

    private String deployBaseTemplate
    private List<String> rootSettings
    private List<String> dependencies
    // 所有被依赖的项目
    private Set<String> allBeDependencies
    private String projectMaintainer

    private def globalConfig
    private def virtualEnvConfig
    private def cmdbConfig

    private def route
    private def idMaker
    private def storage
    private def rocketMq
    private def logcollector
    private def redis
    private def distributedLock
    private def asyncTask

    // 是否需要构建
    boolean needBuild
    // 构建状态: not, ing, successed, failed
    String status = "not"

    // 是否需要回滚
    boolean needRollback

    String getProjectName() {
        return projectName
    }

    void setProjectName(String projectName) {
        this.projectName = projectName
    }

    String getDeployImageName() {
        return deployImageName
    }

    void setDeployImageName(String deployImageName) {
        this.deployImageName = deployImageName
    }

    String getCodeType() {
        return codeType
    }

    void setCodeType(String codeType) {
        this.codeType = codeType
    }

    String getCodeDir() {
        return codeDir
    }

    void setCodeDir(String codeDir) {
        this.codeDir = codeDir
    }

    String getBuildType() {
        return buildType
    }

    void setBuildType(String buildType) {
        this.buildType = buildType
    }

    String getBuildDir() {
        return buildDir
    }

    void setBuildDir(String buildDir) {
        this.buildDir = buildDir
    }

    List<String> getRootSettings() {
        return rootSettings
    }

    void setRootSettings(List<String> rootSettings) {
        this.rootSettings = rootSettings
    }

    List<String> getDependencies() {
        return dependencies
    }

    void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies
    }

    Set<String> getAllBeDependencies() {
        return allBeDependencies
    }

    void setAllBeDependencies(Set<String> allBeDependencies) {
        this.allBeDependencies = allBeDependencies
    }

    boolean getNeedBuild() {
        return needBuild
    }

    void setNeedBuild(boolean needBuild) {
        this.needBuild = needBuild
    }

    String getStatus() {
        return status
    }

    void setStatus(String status) {
        this.status = status
    }

    String getDeployYamlFileName() {
        return deployYamlFileName
    }

    void setDeployYamlFileName(String deployYamlFileName) {
        this.deployYamlFileName = deployYamlFileName
    }

    String getBuildBaseImage() {
        return buildBaseImage
    }

    void setBuildBaseImage(String buildBaseImage) {
        this.buildBaseImage = buildBaseImage
    }

    String getProjectMaintainer() {
        return this.projectMaintainer
    }

    void setProjectMaintainer(String projectMaintainer) {
        this.projectMaintainer = projectMaintainer
    }

    String getDeployDeliveryGroup() {
        return deployDeliveryGroup
    }

    void setDeployDeliveryGroup(String deployDeliveryGroup) {
        this.deployDeliveryGroup = deployDeliveryGroup
    }

    String getArgocdGitUrl() {
        return argocdGitUrl
    }

    void setArgocdGitUrl(String argocdGitUrl) {
        this.argocdGitUrl = argocdGitUrl
    }

    String getDeployBaseTemplate() {
        return deployBaseTemplate
    }

    void setDeployBaseTemplate(String deployBaseTemplate) {
        this.deployBaseTemplate = deployBaseTemplate
    }

    def getGlobalConfig() {
        return globalConfig
    }

    void setGlobalConfig(def globalConfig) {
        this.globalConfig = globalConfig
    }

    def getVirtualEnvConfig() {
        return virtualEnvConfig
    }

    void setVirtualEnvConfig(virtualEnvConfig) {
        this.virtualEnvConfig = virtualEnvConfig
    }

    def getCmdbConfig() {
        return cmdbConfig
    }

    void setCmdbConfig(def cmdbConfig) {
        this.cmdbConfig = cmdbConfig
    }

    def getRoute() {
        return route
    }

    void setRoute(route) {
        this.route = route
    }

    def getIdMaker() {
        return idMaker
    }

    void setIdMaker(idMaker) {
        this.idMaker = idMaker
    }

    def getStorage() {
        return storage
    }

    void setStorage(storage) {
        this.storage = storage
    }

    def getRocketMq() {
        return rocketMq
    }

    void setRocketMq(rocketMq) {
        this.rocketMq = rocketMq
    }

    def getLogcollector() {
        return logcollector
    }

    void setLogcollector(logcollector) {
        this.logcollector = logcollector
    }

    def getRedis() {
        return redis
    }

    void setRedis(redis) {
        this.redis = redis
    }

    def getDistributedLock() {
        return distributedLock
    }

    void setDistributedLock(distributedLock) {
        this.distributedLock = distributedLock
    }

    def getAsyncTask() {
        return asyncTask
    }

    void setAsyncTask(asyncTask) {
        this.asyncTask = asyncTask
    }

    boolean getNeedRollback() {
        return needRollback
    }

    void setNeedRollback(boolean needRollback) {
        this.needRollback = needRollback
    }

    Map getDeployVariables() {
        return deployVariables
    }

    void setDeployVariables(Map variables) {
        this.deployVariables = variables
    }

    String getPackageTool() {
        return packageTool
    }

    void setPackageTool(String packageTool) {
        this.packageTool = packageTool
    }

    String getTargetStaticDir() {
        return targetStaticDir
    }

    void setTargetStaticDir(String targetStaticDir) {
        this.targetStaticDir = targetStaticDir
    }
}
