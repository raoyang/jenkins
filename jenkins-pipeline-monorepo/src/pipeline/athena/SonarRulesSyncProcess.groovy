package pipeline.athena


import entity.Context
import pipeline.BaseProcess

/**
 *  SONAR规则同步流程
 */
class SonarRulesSyncProcess extends BaseProcess {

    private List<String> languageList = ['cs', 'css', 'flex', 'web', 'java', 'js', 'py', 'ts', 'xml']
    private Map<String, String> sonarMap = ['cs'  : 'authine+c%23+rules',
                                            'css' : 'authine+css+rules',
                                            'flex': 'authine+flex+rules',
                                            'web' : 'authine+html+rules',
                                            'java': 'lateinos+java+rules+base',
                                            'js'  : 'authine+javascript+rules',
                                            'py'  : 'authine+pythonrules',
                                            'ts'  : 'authine+typescripts+rules',
                                            'xml' : 'authine+xml+rules']

    private String RDUrl = "http://sonar.gitlab-net.svc:80/"
    private String RDToken = "87c68f0c3696038fe9b853f7612cbf6e28853156"
    private String monorepoUrl = "http://sonar-monorepo-svc.athena.svc:9000/"
    private String monorepoToken = "2a715efb6a556afec675d43fd0fb2059f85a8b02"
    private String localScanUrl = "http://sonar-local-scan-svc.athena.svc:9000/"
    private String localScanToken = "80110c52cefb44cadd1ca4dd6e235ef8574762b0"

    private boolean hasErrorFlag = false

    SonarRulesSyncProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return null
    }

    @Override
    protected void execute() {
        for (String language : this.languageList) {
            this.context.jenkins.stage("同步${language}语言规则") {
                this.syncRule(language)
            }
        }

        if (this.hasErrorFlag) {
            this.finishedNotifyCmd.setNotifyToGlobalFlag(true)
            this.finishedNotifyCmd.setErrorNotify(true)
            this.processFailed = true
        }
    }

    private void syncRule(String language) {

        String params = "language=${language}&qualityProfile=${this.sonarMap.get(language)}"

        String xmlFile = this.context.jenkins.sh(script:
                "curl -u ${this.RDToken}: \"${this.RDUrl}api/qualityprofiles/backup?${params}\"", returnStdout: true).trim()
        if (xmlFile == null || xmlFile == '') {
            this.finishedNotifyCmd.notifyMsg["${language}语言规则备份"] = "失败!!!"
            this.hasErrorFlag = true
            return
        }
        this.context.jenkins.echo "${language}类型的规则备份成功"

        this.context.jenkins.writeFile file: "./${language}.xml", text: xmlFile

        restore(this.monorepoToken, this.monorepoUrl, language)

        restore(this.localScanToken, this.localScanUrl, language)

    }

    private void restore(String token, String url, String language) {
        String msgLocal = this.context.jenkins.sh(script:
                "curl -v POST -u ${token}: \"${url}api/qualityprofiles/restore\"" +
                        " --form backup=@${language}.xml", returnStdout: true).trim()
        this.context.jenkins.echo "返回结果为${msgLocal}"
        if (msgLocal.contains('HTTP Status')) {
            this.finishedNotifyCmd.notifyMsg["同步${language}语言的规则"] = "失败!!!"
            this.hasErrorFlag = true
        } else {
            this.context.jenkins.echo "${language}规则同步成功"
        }
    }

}
