package entity

class GitHook implements Serializable {
    private Integer id
    private String url
    private String createdAt
    private boolean pushEvents
    private boolean tagPushEvents
    private boolean mergeRequestsEvents
    private boolean repositoryUpdateEvents
    private boolean enableSslVerification
    private Integer projectId
    private boolean issuesEvents
    private boolean confidentialIssuesEvents
    private boolean noteEvents
    private boolean confidentialNoteEvents
    private boolean pipelineEvents
    private boolean wikiPageEvents
    private boolean deploymentEvents
    private boolean jobEvents
    private boolean releasesEvents
    private String pushEventsBranchFilter

    GitHook() {
        this.pushEvents = true
        this.tagPushEvents = false
        this.mergeRequestsEvents = false
        this.repositoryUpdateEvents = false
        this.enableSslVerification = false
        this.issuesEvents = false
        this.confidentialIssuesEvents = false
        this.noteEvents = false
        this.confidentialNoteEvents = false
        this.pipelineEvents = false
        this.wikiPageEvents = false
        this.deploymentEvents = false
        this.jobEvents = false
        this.releasesEvents = false
    }

    GitHook(Integer id, String url, String createdAt, boolean pushEvents, boolean tagPushEvents, boolean mergeRequestsEvents, boolean repositoryUpdateEvents, boolean enableSslVerification, Integer projectId, boolean issuesEvents, boolean confidentialIssuesEvents, boolean noteEvents, boolean confidentialNoteEvents, boolean pipelineEvents, boolean wikiPageEvents, boolean deploymentEvents, boolean jobEvents, boolean releasesEvents, String pushEventsBranchFilter) {
        this.id = id
        this.url = url
        this.createdAt = createdAt
        this.pushEvents = pushEvents
        this.tagPushEvents = tagPushEvents
        this.mergeRequestsEvents = mergeRequestsEvents
        this.repositoryUpdateEvents = repositoryUpdateEvents
        this.enableSslVerification = enableSslVerification
        this.projectId = projectId
        this.issuesEvents = issuesEvents
        this.confidentialIssuesEvents = confidentialIssuesEvents
        this.noteEvents = noteEvents
        this.confidentialNoteEvents = confidentialNoteEvents
        this.pipelineEvents = pipelineEvents
        this.wikiPageEvents = wikiPageEvents
        this.deploymentEvents = deploymentEvents
        this.jobEvents = jobEvents
        this.releasesEvents = releasesEvents
        this.pushEventsBranchFilter = pushEventsBranchFilter
    }

    Integer getId() {
        return id
    }

    GitHook setId(Integer id) {
        this.id = id
        return this
    }

    String getUrl() {
        return url
    }

    GitHook setUrl(String url) {
        this.url = url
        return this
    }

    String getCreatedAt() {
        return createdAt
    }

    GitHook setCreatedAt(String createdAt) {
        this.createdAt = createdAt
        return this
    }

    boolean getPushEvents() {
        return pushEvents
    }

    GitHook setPushEvents(boolean pushEvents) {
        this.pushEvents = pushEvents
        return this
    }

    boolean getTagPushEvents() {
        return tagPushEvents
    }

    GitHook setTagPushEvents(boolean tagPushEvents) {
        this.tagPushEvents = tagPushEvents
        return this
    }

    boolean getMergeRequestsEvents() {
        return mergeRequestsEvents
    }

    GitHook setMergeRequestsEvents(boolean mergeRequestsEvents) {
        this.mergeRequestsEvents = mergeRequestsEvents
        return this
    }

    boolean getRepositoryUpdateEvents() {
        return repositoryUpdateEvents
    }

    GitHook setRepositoryUpdateEvents(boolean repositoryUpdateEvents) {
        this.repositoryUpdateEvents = repositoryUpdateEvents
        return this
    }

    boolean getEnableSslVerification() {
        return enableSslVerification
    }

    GitHook setEnableSslVerification(boolean enableSslVerification) {
        this.enableSslVerification = enableSslVerification
        return this
    }

    Integer getProjectId() {
        return projectId
    }

    GitHook setProjectId(Integer projectId) {
        this.projectId = projectId
        return this
    }

    boolean getIssuesEvents() {
        return issuesEvents
    }

    GitHook setIssuesEvents(boolean issuesEvents) {
        this.issuesEvents = issuesEvents
        return this
    }

    boolean getConfidentialIssuesEvents() {
        return confidentialIssuesEvents
    }

    GitHook setConfidentialIssuesEvents(boolean confidentialIssuesEvents) {
        this.confidentialIssuesEvents = confidentialIssuesEvents
        return this
    }

    boolean getNoteEvents() {
        return noteEvents
    }

    GitHook setNoteEvents(boolean noteEvents) {
        this.noteEvents = noteEvents
        return this
    }

    boolean getConfidentialNoteEvents() {
        return confidentialNoteEvents
    }

    GitHook setConfidentialNoteEvents(boolean confidentialNoteEvents) {
        this.confidentialNoteEvents = confidentialNoteEvents
        return this
    }

    boolean getPipelineEvents() {
        return pipelineEvents
    }

    GitHook setPipelineEvents(boolean pipelineEvents) {
        this.pipelineEvents = pipelineEvents
        return this
    }

    boolean getWikiPageEvents() {
        return wikiPageEvents
    }

    GitHook setWikiPageEvents(boolean wikiPageEvents) {
        this.wikiPageEvents = wikiPageEvents
        return this
    }

    boolean getDeploymentEvents() {
        return deploymentEvents
    }

    GitHook setDeploymentEvents(boolean deploymentEvents) {
        this.deploymentEvents = deploymentEvents
        return this
    }

    boolean getJobEvents() {
        return jobEvents
    }

    GitHook setJobEvents(boolean jobEvents) {
        this.jobEvents = jobEvents
        return this
    }

    boolean getReleasesEvents() {
        return releasesEvents
    }

    GitHook setReleasesEvents(boolean releasesEvents) {
        this.releasesEvents = releasesEvents
        return this
    }

    String getPushEventsBranchFilter() {
        return pushEventsBranchFilter
    }

    GitHook setPushEventsBranchFilter(String pushEventsBranchFilter) {
        this.pushEventsBranchFilter = pushEventsBranchFilter
        return this
    }

    Map beanToMap() {
        Map gitHook = new HashMap()
//        gitHook.put("id",this.id)
        gitHook.put("url",this.url)
        gitHook.put("created_at",this.createdAt)
        gitHook.put("push_events",this.pushEvents)
        gitHook.put("tag_push_events",this.tagPushEvents)
        gitHook.put("merge_requests_events",this.mergeRequestsEvents)
        gitHook.put("repository_update_events",this.repositoryUpdateEvents)
        gitHook.put("enable_ssl_verification",this.enableSslVerification)
        gitHook.put("project_id",this.projectId)
        gitHook.put("issues_events",this.issuesEvents)
        gitHook.put("confidential_issues_events",this.confidentialIssuesEvents)
        gitHook.put("note_events",this.noteEvents)
        gitHook.put("confidential_note_events",this.confidentialIssuesEvents)
        gitHook.put("pipeline_events",this.pipelineEvents)
        gitHook.put("wiki_page_events",this.wikiPageEvents)
        gitHook.put("deployment_events",this.deploymentEvents)
        gitHook.put("job_events",this.jobEvents)
        gitHook.put("releases_events",this.releasesEvents)
        gitHook.put("push_events_branch_filter",this.pushEventsBranchFilter)
        return gitHook
    }
}
