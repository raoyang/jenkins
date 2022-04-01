package cmd

import cmd.utils.GitUtils

class DeleteGitHookCmd extends AbstractCmd<String> {

    private String gitUrl
    private String projectPath
    private Integer projectId
    private String token
    private Integer hookId

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2

    private String resultImpl

    DeleteGitHookCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("gitUrl", this.gitUrl)
        checkEmpty("projectPath", this.projectPath)
        checkEmpty("token", this.token)

        def path
        if (this.projectId != null) {
            path = this.projectId
        } else if (this.projectPath != null && this.projectPath != '') {
            path = GitUtils.fixPath(this.projectPath)
        } else {
            return null
        }
        def text = ""
        if (this.retryTimes > 0) {
            this.context.jenkins.retry(this.retryTimes + 1) {
                text = this.context.jenkins.sh(script: """
                        curl --request DELETE --header "PRIVATE-TOKEN:${this.token}" "${this.gitUrl}/api/v4/projects/${path}/hooks/${this.hookId}"
                    """, returnStdout: true)
            }
        } else {
            text = this.context.jenkins.sh(script: """
                    curl --request DELETE --header "PRIVATE-TOKEN:${this.token}" "${this.gitUrl}/api/v4/projects/${path}/hooks/${this.hookId}"
                """, returnStdout: true)
        }

        this.resultImpl = text
        return this
    }

    @Override
    String getResult() {
        return this.resultImpl
    }

    String getGitUrl() {
        return gitUrl
    }

    DeleteGitHookCmd setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl
        return this
    }

    String getProjectPath() {
        return projectPath
    }

    DeleteGitHookCmd setProjectPath(String projectPath) {
        this.projectPath = projectPath
        return this
    }

    Integer getProjectId() {
        return projectId
    }

    DeleteGitHookCmd setProjectId(Integer projectId) {
        this.projectId = projectId
        return this
    }

    String getToken() {
        return token
    }

    DeleteGitHookCmd setToken(String token) {
        this.token = token
        return this
    }

    Integer getHookId() {
        return hookId
    }

    DeleteGitHookCmd setHookId(Integer hookId) {
        this.hookId = hookId
        return this
    }

    int getRetryTimes() {
        return retryTimes
    }

    DeleteGitHookCmd setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes
        return this
    }
}
