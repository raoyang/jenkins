package cmd

import cmd.utils.GitUtils
import entity.GitHook
import groovy.json.JsonSlurper

class GetAllGitHookCmd extends AbstractCmd<List<GitHook>> {

    private String gitUrl
    private String projectPath
    private Integer projectId
    private String token

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2

    private List<GitHook> resultImpl

    GetAllGitHookCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("gitUrl", this.gitUrl)
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
                        curl --header "PRIVATE-TOKEN:${this.token}" "${this.gitUrl}/api/v4/projects/${path}/hooks"
                    """, returnStdout: true)
            }
        } else {
            text = this.context.jenkins.sh(script: """
                    curl --header "PRIVATE-TOKEN:${this.token}" "${this.gitUrl}/api/v4/projects/${path}/hooks"
                """, returnStdout: true)
        }

        def jsonSlurper = new JsonSlurper()
        List<Map> gitHookList = jsonSlurper.parseText(text)
        List<GitHook> resultList = new ArrayList<>()
        for(Map gitHook : gitHookList){
            resultList.add(mapToBean(gitHook))
        }
        this.resultImpl = resultList
        return this
    }


    @Override
    List<GitHook> getResult() {
        return this.resultImpl
    }

    String getProjectPath() {
        return projectPath
    }

    GetAllGitHookCmd setProjectPath(String projectPath) {
        this.projectPath = projectPath
        return this
    }

    Integer getProjectId() {
        return projectId
    }

    GetAllGitHookCmd setProjectId(Integer projectId) {
        this.projectId = projectId
        return this
    }

    String getGitUrl() {
        return gitUrl
    }

    GetAllGitHookCmd setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl
        return this
    }

    String getToken() {
        return token
    }

    GetAllGitHookCmd setToken(String token) {
        this.token = token
        return this
    }

    private GitHook mapToBean(Map map) {
        GitHook gitHook = new GitHook()
        gitHook.setId(map.get("id"))
        gitHook.setUrl(map.get("url"))
        gitHook.setCreatedAt(map.get("created_at"))
        gitHook.setPushEvents(map.get("push_events"))
        gitHook.setTagPushEvents(map.get("tag_push_events"))
        gitHook.setMergeRequestsEvents(map.get("merge_requests_events"))
        gitHook.setRepositoryUpdateEvents(map.get("repository_update_events"))
        gitHook.setEnableSslVerification(map.get("enable_ssl_verification"))
        gitHook.setProjectId(map.get("project_id"))
        gitHook.setIssuesEvents(map.get("issues_events"))
        gitHook.setConfidentialIssuesEvents(map.get("confidential_issues_events"))
        gitHook.setNoteEvents(map.get("note_events"))
        gitHook.setConfidentialNoteEvents(map.get("confidential_note_events"))
        gitHook.setPipelineEvents(map.get("pipeline_events"))
        gitHook.setWikiPageEvents(map.get("wiki_page_events"))
        gitHook.setDeploymentEvents(map.get("deployment_events"))
        gitHook.setJobEvents(map.get("job_events"))
        gitHook.setReleasesEvents(map.get("releases_events"))
        gitHook.setPushEventsBranchFilter(map.get("push_events_branch_filter"))
        return gitHook
    }

    int getRetryTimes() {
        return retryTimes
    }

    GetAllGitHookCmd setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes
        return this
    }
}
