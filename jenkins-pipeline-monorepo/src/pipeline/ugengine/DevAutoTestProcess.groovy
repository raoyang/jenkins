package pipeline.ugengine

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess
import pipeline.ugengine.autotest.AutoTestExecuteCMD
import pipeline.ugengine.autotest.UploadReportHtmlCMD

class DevAutoTestProcess extends BaseProcess {
    DevAutoTestProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [UgEngineProjectNames.ENGINE_AUTOTEST]
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        ProjectItemConfig config_autotest_job = context.projectsConfig.get(UgEngineProjectNames.ENGINE_AUTOTEST)

        this.finishedNotifyCmd.addToNotifyUserList(config_autotest_job.projectMaintainer)

        this.context.jenkins.stage("配置检查") {
            this.projectConfigCheck(config_autotest_job, DeployCluster.UG_DEV)
        }

        this.context.jenkins.stage("代码扫描") {
            sonarCheck(config_autotest_job)
        }

        //执行测试
        this.context.jenkins.stage("执行场景化测试") {
            AutoTestExecuteCMD test_executor = new AutoTestExecuteCMD(context)
            String executeResult = test_executor.setCluster(DeployCluster.UG_TEST)
                    .setConfig_autotest(config_autotest_job)
                    .setProjectName(config_autotest_job.projectName)
                    .setRunEnv("test")
                    .execute()
                    .getResult()
            this.context.jenkins.echo "自动化测试结果：${executeResult}"

            String reportHtml = uploadReportHtml(config_autotest_job)
            finishedNotifyCmd.buttonUrl["自动化测试报告"] = reportHtml
        }
    }

    private String uploadReportHtml(ProjectItemConfig config_autotest){
        UploadReportHtmlCMD uploadReportHtmlCMD = new UploadReportHtmlCMD(this.context)
        return uploadReportHtmlCMD.setProjectName(config_autotest.projectName)
                .execute().getResult()
    }
}
