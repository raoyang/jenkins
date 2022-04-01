package cmd

import entity.Context

class FileUploadCmd extends AbstractCmd<String> {

    private String deliveryGroup
    private String projectName
    private String filePath
    private String resultUrl

    FileUploadCmd(Context context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("projectName", this.projectName)
        checkEmpty("filePath", this.filePath)
        checkEmpty("deliveryGroup", this.deliveryGroup)

        String[] str = this.filePath.split('/')
        String fileName = str[str.size() - 1]
        String fileDir = this.filePath.replace("/${fileName}",'')
        String cloudDir = "${this.deliveryGroup}/${this.projectName}/${this.context.jenkins.env.BUILD_ID}"
        this.resultUrl = new OssUploadCmd(this.context)
                .setFileDir(fileDir)
                .setFileName(fileName)
                .setCloudDir(cloudDir)
                .execute().getResult()
        return this
    }

    @Override
    String getResult() {
        return this.resultUrl
    }

    String getDeliveryGroup() {
        return deliveryGroup
    }

    FileUploadCmd setDeliveryGroup(String deliveryGroup) {
        this.deliveryGroup = deliveryGroup
        return this
    }

    String getProjectName() {
        return projectName
    }

    FileUploadCmd setProjectName(String projectName) {
        this.projectName = projectName
        return this
    }

    String getFilePath() {
        return filePath
    }

    FileUploadCmd setFilePath(String filePath) {
        this.filePath = filePath
        return this
    }
}
