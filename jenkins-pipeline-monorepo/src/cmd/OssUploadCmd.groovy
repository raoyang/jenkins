package cmd

import entity.Context

class OssUploadCmd extends AbstractCmd<String> {
    private String credentialId
    private String ossEndpoint
    private String resourcesUpload = 'oss://ug-pipeline-bucket'
    private String fileDir
    private String fileName = ''
    private String cloudDir
    private Boolean isRecursive = false
    private String signUrl

    OssUploadCmd(Context context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("fileDir", this.fileDir)
        checkEmpty("cloudDir", this.cloudDir)

        if (this.ossEndpoint == null || this.ossEndpoint == '') {
            this.ossEndpoint = new GetGlobalTokenCmd(this.context)
                    .setTokenId("DevOssEndpoint")
                    .execute()
                    .getResult()
        }
        if (this.credentialId == null || this.credentialId == '') {
            this.credentialId = this.context.jenkins.params.ossCredentialId
        }
        this.context.jenkins.withCredentials([this.context.jenkins.usernamePassword(credentialsId: "${this.credentialId}", usernameVariable: 'OSS_KEY', passwordVariable: 'OSS_SECRET')]) {
            String Cmd = "ossutil64 cp"
            if (this.isRecursive) {
                Cmd = "${Cmd} -r"
                this.context.jenkins.sh """
                if [ ! -d '${this.fileDir}' ];then
                    echo "ERROR: ${this.fileDir}: no such directory"
                    exit 1
                else
                    ${Cmd} ${this.fileDir} ${this.resourcesUpload}/${this.cloudDir}/ --endpoint ${this.ossEndpoint} --access-key-id ${this.context.jenkins.env.OSS_KEY} --access-key-secret ${this.context.jenkins.env.OSS_SECRET}
                fi
            """
            } else {
                this.context.jenkins.sh """
                if [ ! -f '${this.fileDir}/${this.fileName}' ];then
                    echo "ERROR: ${this.fileDir}/${this.fileName}: no such file"
                    exit 1
                else
                    ${Cmd} ${this.fileDir}/${this.fileName} ${this.resourcesUpload}/${this.cloudDir}/ --endpoint ${this.ossEndpoint} --access-key-id ${this.context.jenkins.env.OSS_KEY} --access-key-secret ${this.context.jenkins.env.OSS_SECRET}
                fi
            """
                String logFile = new Date().format('yyMMddHHmm') + '.txt'
                this.context.jenkins.sh "ossutil64 sign ${this.resourcesUpload}/${this.cloudDir}/${this.fileName} --timeout 3600 --endpoint ${this.ossEndpoint} --access-key-id ${this.context.jenkins.env.OSS_KEY} --access-key-secret ${this.context.jenkins.env.OSS_SECRET} > ${logFile}"
                this.signUrl = this.context.jenkins.sh(script: "head -n 1 ${logFile}", returnStdout: true).trim()
            }
            return this
        }
    }

    @Override
    String getResult() {
        return this.signUrl
    }

    String getCredentialId() {
        return credentialId
    }

    OssUploadCmd setCredentialId(String credentialId) {
        this.credentialId = credentialId
        return this
    }

    String getOssEndpoint() {
        return ossEndpoint
    }

    OssUploadCmd setOssEndpoint(String ossEndpoint) {
        this.ossEndpoint = ossEndpoint
        return this
    }

    String getResourcesUpload() {
        return resourcesUpload
    }

    OssUploadCmd setResourcesUpload(String resourcesUpload) {
        this.resourcesUpload = resourcesUpload
        return this
    }

    String getFileDir() {
        return fileDir
    }

    OssUploadCmd setFileDir(String fileDir) {
        this.fileDir = fileDir
        return this
    }

    String getFileName() {
        return fileName
    }

    OssUploadCmd setFileName(String fileName) {
        this.fileName = fileName
        return this
    }

    String getCloudDir() {
        return cloudDir
    }

    OssUploadCmd setCloudDir(String cloudDir) {
        this.cloudDir = cloudDir
        return this
    }

    Boolean getIsRecursive() {
        return isRecursive
    }

    OssUploadCmd setIsRecursive(Boolean isRecursive) {
        this.isRecursive = isRecursive
        return this
    }
}
