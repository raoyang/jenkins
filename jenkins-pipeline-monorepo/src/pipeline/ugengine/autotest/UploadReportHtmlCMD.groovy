package pipeline.ugengine.autotest

import cmd.AbstractCmd
import cmd.FileUploadCmd
import entity.Context

import java.text.SimpleDateFormat

class UploadReportHtmlCMD extends AbstractCmd<Object> {
    UploadReportHtmlCMD(Context context){
        super(context)
    }

    String getProjectName() {
        return projectName
    }

    UploadReportHtmlCMD setProjectName(String projectName) {
        this.projectName = projectName
        return this
    }
    String projectName

    String report_path

    @Override
    AbstractCmd execute() {
        checkEmpty("projectName", this.projectName)

        String  currentFileList= this.context.jenkins.sh(script:  " pwd && ls -l", returnStdout: true)
        this.context.jenkins.echo "重命名report.html前，currentFileList =${currentFileList}"

        //重命名文件名称为report_{日期}.html
        String currentDateString = new SimpleDateFormat("yyyyMMddHHmm").format(new Date())
        String newReportName = "report_${currentDateString}.html"
        this.context.jenkins.sh(script:  " mv report.html ${newReportName}")
        currentFileList= this.context.jenkins.sh(script:  " pwd && ls -l", returnStdout: true)
        this.context.jenkins.echo "重命名report.html后，currentFileList =${currentFileList}"

        //读取测试报告链接
        String reportHtmlPath = "./${newReportName}"
        this.context.jenkins.echo "自动化测试结果path：reportHtmlPath=${reportHtmlPath}"
        this.report_path = new FileUploadCmd(this.context)
                .setFilePath(reportHtmlPath)
                .setProjectName(this.projectName)
                .setDeliveryGroup("ug.engine")
                .execute()
                .getResult()

        this.context.jenkins.echo "自动化测试结果链接：reportUrl = ${this.report_path}"
        return this
    }

    @Override
    Object getResult() {
        return this.report_path
    }
}
