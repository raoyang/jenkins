package cmd


import entity.GitProjectMsg
import groovy.json.JsonSlurper

class GetGroupProjCmd extends AbstractCmd<List<GitProjectMsg>> {

    private Integer groupId
    private String gitUrl
    private String accessToken

    private List<GitProjectMsg> resultImpl

    GetGroupProjCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        List<GitProjectMsg> projectList = new ArrayList()
        boolean finished = false
        int page = 1
        while (!finished) {
            String text = this.context.jenkins.sh(script:
                    "curl --request GET --header 'PRIVATE-TOKEN:${this.accessToken}' " +
                            "'${this.gitUrl}/api/v4/groups/${this.groupId}/projects?simple=true&page=${page}&per_page=100&include_subgroups=true'", returnStdout: true)
            this.context.jenkins.echo "${text}"
            def jsonSlurper = new JsonSlurper()
            List<Map> mapList = jsonSlurper.parseText(text)
            List<GitProjectMsg> tmpList = new ArrayList<>()
            for (Map msg : mapList) {
                tmpList.add(mapToBean(msg))
            }
            projectList.addAll(tmpList)
            if (tmpList.size() != 100) {
                finished = true
            }
            page++
        }
        this.resultImpl = projectList
        return this
    }

    @Override
    List<GitProjectMsg> getResult() {
        return this.resultImpl
    }

    Integer getGroupId() {
        return groupId
    }

    GetGroupProjCmd setGroupId(Integer groupId) {
        this.groupId = groupId
        return this
    }

    String getGitUrl() {
        return gitUrl
    }

    GetGroupProjCmd setGitUrl(String gitUrl) {
        this.gitUrl = gitUrl
        return this
    }

    String getAccessToken() {
        return accessToken
    }

    GetGroupProjCmd setAccessToken(String accessToken) {
        this.accessToken = accessToken
        return this
    }

    private GitProjectMsg mapToBean(Map map) {
        GitProjectMsg gitProjectMsg = new GitProjectMsg()
        gitProjectMsg.setName(map.get("name"))
        gitProjectMsg.setDescription(map.get("description"))
        gitProjectMsg.setNamespacePath(map.get("path_with_namespace"))
        gitProjectMsg.setUrl(map.get("http_url_to_repo"))
        return gitProjectMsg
    }
}