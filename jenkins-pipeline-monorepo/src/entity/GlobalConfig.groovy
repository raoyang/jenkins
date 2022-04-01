package entity

/**
 * 全局配置
 */
class GlobalConfig implements Serializable {

    private String deliveryGroup
    private String defaultArgocdGitUrl
    private String defaultRegistry
    private String defaultImagePath
    private String dingTalkAccessToken
    private String dingTalkSecret
    private String gitlabUrl
    private String gitlabProjectPath
    private String gitlabApiTokenId
    private String gitlabJenkinsMultibranchHookBaseUrl
    private String gitlabJenkinsMultibranchHookSecret
    private String teamLeader

    private Map<String, String> userPhones

    private def userDetails

    String getDeliveryGroup() {
        return deliveryGroup
    }

    void setDeliveryGroup(String deliveryGroup) {
        this.deliveryGroup = deliveryGroup
    }

    String getDefaultArgocdGitUrl() {
        return defaultArgocdGitUrl
    }

    void setDefaultArgocdGitUrl(String defaultArgocdGitUrl) {
        this.defaultArgocdGitUrl = defaultArgocdGitUrl
    }

    String getDefaultRegistry() {
        return defaultRegistry
    }

    void setDefaultRegistry(String defaultRegistry) {
        this.defaultRegistry = defaultRegistry
    }

    String getDefaultImagePath() {
        return defaultImagePath
    }

    void setDefaultImagePath(String defaultImagePath) {
        this.defaultImagePath = defaultImagePath
    }

    String getDingTalkAccessToken() {
        return this.dingTalkAccessToken
    }

    void setDingTalkAccessToken(String dingTalkAccessToken) {
        this.dingTalkAccessToken = dingTalkAccessToken
    }

    String getDingTalkSecret() {
        return this.dingTalkSecret
    }

    void setDingTalkSecret(String dingTalkSecret) {
        this.dingTalkSecret = dingTalkSecret
    }

    Map<String, String> getUserPhones() {
        return userPhones
    }

    void setUserPhones(Map<String, String> userPhones) {
        this.userPhones = userPhones
    }

    String getGitlabApiTokenId() {
        return this.gitlabApiTokenId
    }

    void setGitlabApiTokenId(String gitlabApiTokenId) {
        this.gitlabApiTokenId = gitlabApiTokenId
    }

    String getTeamLeader() {
        return this.teamLeader
    }

    void setTeamLeader(String teamLeader) {
        this.teamLeader = teamLeader
    }

    String getGitlabUrl() {
        return this.gitlabUrl
    }

    void setGitlabUrl(String gitlabUrl) {
        this.gitlabUrl = gitlabUrl
    }

    String getGitlabProjectPath() {
        return gitlabProjectPath
    }

    void setGitlabProjectPath(String gitlabProjectPath) {
        this.gitlabProjectPath = gitlabProjectPath
    }

    String getGitlabJenkinsMultibranchHookBaseUrl() {
        return gitlabJenkinsMultibranchHookBaseUrl
    }

    void setGitlabJenkinsMultibranchHookBaseUrl(String gitlabJenkinsMultibranchHookBaseUrl) {
        this.gitlabJenkinsMultibranchHookBaseUrl = gitlabJenkinsMultibranchHookBaseUrl
    }

    String getGitlabJenkinsMultibranchHookSecret() {
        return gitlabJenkinsMultibranchHookSecret
    }

    void setGitlabJenkinsMultibranchHookSecret(String gitlabJenkinsMultibranchHookSecret) {
        this.gitlabJenkinsMultibranchHookSecret = gitlabJenkinsMultibranchHookSecret
    }

    def getUserDetails() {
        return userDetails
    }

    void setUserDetails(userDetails) {
        this.userDetails = userDetails
    }
}
