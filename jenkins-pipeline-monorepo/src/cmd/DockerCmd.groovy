package cmd


import java.text.SimpleDateFormat

/**
 * 构建docker镜像命令
 */
class DockerCmd extends AbstractCmd<String> {

    private String codeType
    private String targetDir
    private String registry
    private String imagePath
    private String repository
    private String tag
    private String baseImage
    private boolean hasFilesJson = false

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2

    private String resultImpl

    DockerCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("codeType", this.codeType)
        checkEmpty("targetDir", this.targetDir)
        checkEmpty("registry", this.registry)
        checkEmpty("imagePath", this.imagePath)
        checkEmpty("repository", this.repository)
        checkEmpty("tag", this.tag)
        checkEmpty("baseImage", this.baseImage)

        // 构建镜像版本号
        String image = "${this.registry}/${this.imagePath}/${this.repository}:${this.tag}"

        // 构建临时dockerfile文件
        String time = new SimpleDateFormat("yyyyMMddHHmm")
                .format(new Date(this.context.jenkins.currentBuild.startTimeInMillis))
        String tempPath = "/tmp/docker_temp/${time}/${this.repository}"
        this.context.jenkins.sh "mkdir -p ${tempPath}"
        this.context.jenkins.dir("${tempPath}") {
            // 拷贝打包后target文件到当前工作空间
            this.context.jenkins.sh "cp -r ${this.context.jenkins.env.WORKSPACE}/${this.targetDir} ${tempPath}/sources"
            this.context.jenkins.echo "拷贝打包后target文件到当前工作空间: ${this.codeType}/${this.baseImage}"

            // 拷贝镜像模板扩展资源文件到工作空间
            if (this.hasFilesJson) {
                String filesJsonTemplate = this.context.jenkins.libraryResource "dockerTemplates/${this.codeType}/${this.baseImage}/files.json"
                def filesJson = this.context.jenkins.readJSON text: filesJsonTemplate
                def files = filesJson["files"]
                this.context.jenkins.echo "拷贝镜像模板扩展资源文件到工作空间: files=${files}"
                if (files != null && files.size() > 0) {
                    for (def fileItem in files) {
                        String sourceFileName = fileItem.source
                        String targetFileName = fileItem.target

                        String content = this.context.jenkins.libraryResource "dockerTemplates/${this.codeType}/${this.baseImage}/${sourceFileName}"
                        this.context.jenkins.writeFile file: "${tempPath}/${targetFileName}", text: content
                        this.context.jenkins.echo "拷贝镜像模板扩展资源文件到工作空间: fileItem=${fileItem}, targetFilePath=${tempPath}/${targetFileName}, content=${content}"
                    }
                }
            }

            // 处理镜像版本
            String dockerfile = this.context.jenkins.libraryResource "dockerTemplates/${this.codeType}/${this.baseImage}/Dockerfile"
            String makefile = this.context.jenkins.libraryResource "dockerTemplates/${this.codeType}/${this.baseImage}/Makefile"
            String runshfile = this.context.jenkins.libraryResource "dockerTemplates/${this.codeType}/${this.baseImage}/sh/run.sh"
            dockerfile = dockerfile.replace("#registry#", this.registry)
            makefile = makefile.replace("#imageTag#", image)
            this.context.jenkins.writeFile file: "./Dockerfile", text: dockerfile
            this.context.jenkins.writeFile file: "./Makefile", text: makefile
            this.context.jenkins.writeFile file: "./sh/run.sh", text: runshfile

            this.context.jenkins.sh "ls -l"
            this.context.jenkins.echo "格式化文件: Makefile=${makefile}"

            // 制作并推送镜像
            if (this.retryTimes > 0) {
                this.context.jenkins.retry(this.retryTimes + 1) {
                    this.context.jenkins.sh "make"
                }
            } else {
                this.context.jenkins.sh "make"
            }

            if (this.retryTimes > 0) {
                this.context.jenkins.retry(this.retryTimes + 1) {
                    this.context.jenkins.sh "docker push ${image}"
                }
            } else {
                this.context.jenkins.sh "docker push ${image}"
            }

            // 清理临时文件
            this.context.jenkins.sh "rm -rf"
        }

        this.resultImpl = image
        return this
    }

    @Override
    String getResult() {
        return this.resultImpl
    }

    String getCodeType() {
        return codeType
    }

    DockerCmd setCodeType(String codeType) {
        this.codeType = codeType
        return this
    }

    String getTargetDir() {
        return targetDir
    }

    DockerCmd setTargetDir(String targetDir) {
        this.targetDir = targetDir
        return this
    }

    String getRegistry() {
        return registry
    }

    DockerCmd setRegistry(String registry) {
        this.registry = registry
        return this
    }

    String getImagePath() {
        return imagePath
    }

    DockerCmd setImagePath(String imagePath) {
        this.imagePath = imagePath
        return this
    }

    String getRepository() {
        return repository
    }

    DockerCmd setRepository(String repository) {
        this.repository = repository
        return this
    }

    String getTag() {
        return tag
    }

    DockerCmd setTag(String tag) {
        this.tag = tag
        return this
    }

    String getBaseImage() {
        return baseImage
    }

    DockerCmd setBaseImage(String baseImage) {
        this.baseImage = baseImage
        return this
    }

    int getRetryTimes() {
        return retryTimes
    }

    DockerCmd setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes
        return this
    }

    boolean getHasFilesJson() {
        return hasFilesJson
    }

    DockerCmd setHasFilesJson(boolean hasFilesJson) {
        this.hasFilesJson = hasFilesJson
        return this
    }
}
