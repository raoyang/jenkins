package cmd

import cmd.utils.GitUtils
import entity.GitHook

class AddGitHookCmd extends AbstractCmd<String> {

    private String gitUrl
    private String projectPath
    private Integer projectId
    private String accessToken
    private String secretToken
    private GitHook gitHook

    private String path

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2
    private String resultImpl

    AddGitHookCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("gitUrl", this.gitUrl)
        checkEmpty("projectPath", this.projectPath)
        checkEmpty("accessToken", this.accessToken)
        checkEmpty("secretToken", this.secretToken)

        if (this.projectId != null) {
            path = this.projectId
        } else if (this.projectPath != null && this.projectPath != '') {
            path = GitUtils.fixPath(this.projectPath)
        } else {
            this.context.jenkins.error "unkonw path for cmd..."
        }

        if (this.retryTimes > 0) {
            this.context.jenkins.retry(this.retryTimes + 1) {
                this.resultImpl = addGitHook()
            }
        } else {
            this.resultImpl = addGitHook()
        }

        return this
    }

    private def addGitHook() {
        String url = this.gitHook.getUrl()
        boolean pushEvents = this.gitHook.getPushEvents()
        boolean tagPushEvents = this.gitHook.getTagPushEvents()
        boolean mergeRequestsEvents = this.gitHook.getMergeRequestsEvents()
        boolean repositoryUpdateEvents = this.gitHook.getRepositoryUpdateEvents()
        boolean enableSslVerification = this.gitHook.getEnableSslVerification()
        boolean issuesEvents = this.gitHook.issuesEvents
        boolean confidentialIssuesEvents = this.gitHook.confidentialIssuesEvents
        boolean noteEvents = this.gitHook.noteEvents
        boolean confidentialNoteEvents = this.gitHook.getConfidentialNoteEvents()
        boolean pipelineEvents = this.gitHook.getPipelineEvents()
        boolean wikiPageEvents = this.gitHook.getWikiPageEvents()
        boolean deploymentEvents = this.gitHook.getDeploymentEvents()
        boolean jobEvents = this.gitHook.getJobEvents()
        boolean releasesEvents = this.gitHook.getReleasesEvents()
        String pushEventsBranchFilter = this.gitHook.getPushEventsBranchFilter()

        def text = this.context.jenkins.sh(script:
                "curl --request POST --header 'PRIVATE-TOKEN:${this.accessToken}' " +
                        "'${this.gitUrl}/api/v4/projects/${path}/hooks?url=${url}&token=${this.secretToken}&push_event=${pushEvents}" +
                        "&tag_push_events=${tagPushEvents}&merge_requests_evnets=${mergeRequestsEvents}&repository_update_events=${repositoryUpdateEvents}" +
                        "&enable_ssl_verification=${enableSslVerification}&issues_events=${issuesEvents}&confidential_issues_events=${confidentialIssuesEvents}" +
                        "&note_events=${noteEvents}&confidential_note_events=${confidentialNoteEvents}&pipeline_events=${pipelineEvents}" +
                        "&wiki_page_events=${wikiPageEvents}&deployment_events=${deploymentEvents}&job_events=${jobEvents}" +
                        "&releases_events=${releasesEvents}&push_events_branch_filter=${pushEventsBranchFilter}' ", returnStdout: true)
        return text
    }

    @Override
    String getResult() {
        return this.resultImpl
    }

    String getGitUrl() {
        return gitUrl
    }

    AddGitHookCmd setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl
        return this
    }

    String getProjectPath() {
        return projectPath
    }

    AddGitHookCmd setProjectPath(String projectPath) {
        this.projectPath = projectPath
        return this
    }

    Integer getProjectId() {
        return projectId
    }

    AddGitHookCmd setProjectId(Integer projectId) {
        this.projectId = projectId
        return this
    }

    String getAccessToken() {
        return accessToken
    }

    AddGitHookCmd setAccessToken(String accessToken) {
        this.accessToken = accessToken
        return this
    }

    String getSecretToken() {
        return secretToken
    }

    AddGitHookCmd setSecretToken(String secretToken) {
        this.secretToken = secretToken
        return this
    }

    GitHook getGitHook() {
        return gitHook
    }

    AddGitHookCmd setGitHook(GitHook gitHook) {
        this.gitHook = gitHook
        return this
    }

    int getRetryTimes() {
        return retryTimes
    }

    AddGitHookCmd setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes
        return this
    }
}
