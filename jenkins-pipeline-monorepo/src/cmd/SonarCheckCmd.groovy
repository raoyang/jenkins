package cmd

import entity.Context
import entity.ProjectItemConfig
import entity.SonarCheckMsg
import enums.SonarServer

/**
 * 代码检查阶段
 */
class SonarCheckCmd extends AbstractCmd<SonarCheckMsg> {

    private ProjectItemConfig projectItemConfig
    private String gitCommitter
    private SonarServer sonarServer = SonarServer.ATHENA
    private String sonarVersion

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2

    private SonarCheckMsg resultImpl

    SonarCheckCmd(Context context) {
        super(context)
    }


    @Override
    AbstractCmd execute() {
        checkEmpty("gitCommitter", this.gitCommitter)


        if (this.sonarVersion == null || this.sonarVersion == '') {
            this.sonarVersion = new Date().format("yyyy.MM.dd.HH.mm.ss").toString()
        } else {
            this.context.jenkins.echo "本次扫描的版本号是${this.sonarVersion}"
        }

        try {
            this.resultImpl = new SonarCheckMsg()

            if (retryTimes > 0) {
                this.context.jenkins.retry(retryTimes + 1) {
                    doScan()
                }
            } else {
                doScan()
            }
            waitForQualityGate()

        } finally {
            removeSonarFile()
        }
        return this
    }

    private void doScan() {
        String type = this.projectItemConfig.getCodeType()
        if ('java'.equalsIgnoreCase(type)) {
            gradleSonar(this.projectItemConfig)
        } else if ('dotnet'.equalsIgnoreCase(type)) {
            dotnetSonar(this.projectItemConfig)
        } else if ('webapp'.equalsIgnoreCase(type) || 'microapp'.equalsIgnoreCase(type)) {
            frontEndSonar(this.projectItemConfig)
        } else {
            this.context.jenkins.error "不支持代码扫描的项目类型"
        }
    }

    private String generateSonarUrl() {
        return "${this.sonarServer.getBaseUrl()}/project/issues?id=${this.projectItemConfig.projectName}&resolved=false"
    }

    private void waitForQualityGate() {

        this.context.jenkins.timeout(time: 1, unit: 'HOURS') {
            def qg = this.context.jenkins.waitForQualityGate()
            this.context.jenkins.echo "${qg}"
            if (qg.status != 'OK') {
                this.context.jenkins.echo "Status: ${qg.status}"
                this.resultImpl.setErrorMsg("代码扫描结果不通过")
                this.resultImpl.setSuccess(false)
            } else {
                this.resultImpl.setSuccess(true)
            }
        }
    }

    private void removeSonarFile() {
        String type = this.projectItemConfig.getCodeType()
        String codeDir = this.projectItemConfig.getCodeDir()
        if ('java'.equalsIgnoreCase(type)) {
            this.context.jenkins.sh "rm -f ${codeDir}/build/sonar/report-task.txt"
        } else if ('dotnet'.equalsIgnoreCase(type)) {
            this.context.jenkins.sh "rm -f ${codeDir}/.sonarqube/out/.sonar/report-task.txt"
        } else if ('webapp'.equalsIgnoreCase(type) || 'microapp'.equalsIgnoreCase(type)) {
            this.context.jenkins.sh "rm -f ${codeDir}/.scannerwork/report-task.txt"
        }
    }

    private void gradleSonar(ProjectItemConfig projectItemConfig) {
        String projectKey = projectItemConfig.getProjectName()
        String buildScriptDir = projectItemConfig.getCodeDir()
        this.context.jenkins.withSonarQubeEnv(this.sonarServer.getCode()) {
            this.context.jenkins.sh "gradle sonarqube -p ${buildScriptDir} -Dsonar.projectKey=${projectKey} " +
                    "-Dsonar.analysis.commitUser=${this.gitCommitter} -Dsonar.projectName=${projectKey} " +
                    "-Dsonar.projectVersion=${this.sonarVersion} -Dorg.gradle.jvmargs=-Xmx2048m"
            resultImpl.setSonarUrl(generateSonarUrl())
        }
    }

    private void frontEndSonar(ProjectItemConfig projectItemConfig) {
        String projectKey = projectItemConfig.getProjectName()
        String PackageJsonDir = projectItemConfig.getCodeDir()
        this.context.jenkins.withSonarQubeEnv(this.sonarServer.getCode()) {
            this.context.jenkins.sh "cd ${PackageJsonDir} && rm -rf node_modules"
            this.context.jenkins.sh "cd ${PackageJsonDir} && sonar-scanner -Dsonar.projectKey=${projectKey} " +
                    "-Dsonar.projectName=${projectKey} -Dsonar.projectVersion=${this.sonarVersion} -Dsonar.sources=."
            resultImpl.setSonarUrl(generateSonarUrl())
        }
    }

    private void dotnetSonar(ProjectItemConfig projectItemConfig) {
        String projectKey = projectItemConfig.getProjectName()
        String slnScriptDir = "${projectItemConfig.getCodeDir()}"
        this.context.jenkins.withSonarQubeEnv(this.sonarServer.getCode()) {
            String dllPath = '/root/.dotnet/tools/.store/dotnet-sonarscanner/5.4.1/dotnet-sonarscanner/5.4.1/tools/netcoreapp3.0/any/SonarScanner.MSBuild.dll'
            this.context.jenkins.sh "cd ${slnScriptDir} && dotnet ${dllPath} begin /k:${projectKey} " +
                    "/v:sonar.projectVersion=${this.sonarVersion} /d:sonar.dotnet.excludeTestProjects=true " +
                    "&& dotnet build && dotnet ${dllPath} end"
            this.resultImpl.setSonarUrl(generateSonarUrl())
        }
    }

    @Override
    SonarCheckMsg getResult() {
        return this.resultImpl
    }

    String getSonarVersion() {
        return sonarVersion
    }

    SonarCheckCmd setSonarVersion(String sonarVersion) {
        this.sonarVersion = sonarVersion
        return this
    }

    SonarServer getSonarServer() {
        return this.sonarServer
    }

    SonarCheckCmd setSonarServer(SonarServer sonarServer) {
        this.sonarServer = sonarServer
        return this
    }

    ProjectItemConfig getProjectItemConfig() {
        return projectItemConfig
    }

    SonarCheckCmd setProjectItemConfig(ProjectItemConfig projectItemConfig) {
        this.projectItemConfig = projectItemConfig
        return this
    }

    String getGitCommitter() {
        return gitCommitter
    }

    SonarCheckCmd setGitCommitter(String gitCommitter) {
        this.gitCommitter = gitCommitter
        return this
    }

    int getRetryTimes() {
        return retryTimes
    }

    SonarCheckCmd setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes
        return this
    }
}
