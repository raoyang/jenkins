package pipeline.ugengine.autotest

import cmd.AbstractCmd
import cmd.FileUploadCmd
import cmd.JavaPackageCmd
import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster

class AutoTestExecuteCMD extends AbstractCmd<String> {

    ProjectItemConfig config_autotest
    private String libsDir
    private DeployCluster cluster
    private String projectName
    private String executeResult
    private String lateinos_route_proxy = ""


    String getLibsDir() {
        return libsDir
    }

    AutoTestExecuteCMD setLibsDir(String libsDir) {
        this.libsDir = libsDir
    }

    // todo 传入运行环境 runEnv
    // todo 执行的测试计划

    String getRunEnv() {
        return runEnv
    }

    AutoTestExecuteCMD setRunEnv(String runEnv) {
        this.runEnv = runEnv
        return this
    }
    private String runEnv = "" //dev, test

    AutoTestExecuteCMD(Context context) {
        super(context)
        this.lateinos_route_proxy = "http://lateinos-controller-agent-svc.lateinos:7000"
    }

    AbstractCmd setConfig_autotest(ProjectItemConfig config_autotest) {
        this.config_autotest = config_autotest
        return this
    }

    String getProjectName() {
        return projectName
    }

    AutoTestExecuteCMD setProjectName(String projectName) {
        this.projectName = projectName
        return this
    }

    DeployCluster getCluster() {
        return cluster
    }

    AutoTestExecuteCMD setCluster(DeployCluster cluster) {
        this.cluster = cluster
        return this
    }

    @Override
    AbstractCmd execute(){
        checkEmpty( "ProjectItemConfig", this.config_autotest)
        checkEmpty( "Cluster", this.cluster)
        checkEmpty( "ProjectName", this.projectName)

        //打印当前目录
        String currentPath = this.context.jenkins.sh(script: "pwd && ls -l", returnStdout: true)
        this.context.jenkins.echo "当前目录：${currentPath}"
        String workspace = this.context.jenkins.env.WORKSPACE
        this.context.jenkins.echo "workspace：${workspace}"
        this.context.jenkins.sh("cd ${workspace}")
        this.libsDir = getLibsPath(this.config_autotest)
        this.context.jenkins.echo "得到libs路径：${this.libsDir}"
        checkEmpty( "LibsDir", this.libsDir)

        //执行自动化测试
        this.context.jenkins.echo "开始执行自动化测试..."
        String soruceDirFiles = this.context.jenkins.sh(script:  "cd ${this.libsDir} && ls -l", returnStdout: true)
        this.context.jenkins.echo "soruceDirFiles =${soruceDirFiles}"
        try {
            // 执行自动化测试，并把执行log输出到文件，返回进程状态
            this.executeResult = this.context.jenkins.sh (script:  "java " +
                    " -Dlateinos.route.proxy=${lateinos_route_proxy}" +
                    " -Dlateinos.header.inject=X-Request-Cluster=${cluster.getCode()}" +
                    " -jar ${this.context.jenkins.env.WORKSPACE}/${this.libsDir}/${projectName}-1.0.0.jar" +
                    " runEnv=${this.runEnv} > autotestLog ", returnStatus: true)
            this.context.jenkins.echo "自动化测试结果：executeResult=${this.executeResult}"
            String  newFileList= this.context.jenkins.sh(script:  " pwd && ls -l", returnStdout: true)
            this.context.jenkins.echo "newFileList =${newFileList}"

            //打印执行日志
            def executedLog = this.context.jenkins.readFile(file:"autotestLog", encoding: "UTF-8")
            this.context.jenkins.echo "自动化测试日志：log=${executedLog}"
        }
        catch (Exception e) {
            this.executeResult = 1
            this.context.jenkins.echo "Exception: ${e.stackTrace}, ${e.toString()}"
        }
        return this
    }

    private String getLibsPath(ProjectItemConfig config) {
        this.context.jenkins.echo "开始获取jar包路径....."
        this.context.jenkins.echo "config.buildDir: ${config.buildDir}"
        return new JavaPackageCmd(context)
                    .setBuildDir(config.buildDir)
                    .setVersion("1-0-0")
                    .setNeedBootJar(true)
                    .execute().getResult()
    }

    @Override
    String getResult() {
        return this.executeResult
    }
}
