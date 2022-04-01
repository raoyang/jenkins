package cmd

import java.text.SimpleDateFormat

/**
 * 构建应用镜像命令
 * JavaPackageCmd/DotnetPackageCmd + DockerCmd 的组合
 */
class BuildAppImageCmd extends AbstractCmd<String> {

    private String codeType
    private String buildDir
    private String projectName
    private String baseImage
    private String registry
    private String imagePath
    private String packageTool
    private String targetStaticDir

    private String resultImpl

    BuildAppImageCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("codeType", this.codeType)
        checkEmpty("buildDir", this.buildDir)
        checkEmpty("projectName", this.projectName)
        checkEmpty("baseImage", this.baseImage)
        checkEmpty("registry", this.registry)
        checkEmpty("imagePath", this.imagePath)

        // 打包
        String version = new SimpleDateFormat("yyyy.MMddHHmm.")
                .format(new Date(this.context.jenkins.currentBuild.startTimeInMillis))
        version += this.context.jenkins.env.BUILD_NUMBER
        String targetDir = ""
        boolean hasFilesJson = false
        if (this.codeType == "java") {
            if (this.packageTool == "maven") {
                targetDir = new MvnPackageCmd(context)
                        .setBuildDir(this.buildDir)
                        .execute().getResult()
            } else {
                targetDir = new JavaPackageCmd(context)
                        .setBuildDir(this.buildDir)
                        .setVersion(version)
                        .setNeedBootJar(true)
                        .execute().getResult()
            }
        } else if (this.codeType == "dotnet") {
            targetDir = new DotnetPackageCmd(context)
                    .setBuildDir(buildDir)
                    .setVersion(version)
                    .execute().getResult()
        } else if (this.codeType == "webapp") {
            targetDir = new WebappPackageCmd(context)
                    .setBuildDir(buildDir)
                    .setVersion(version)
                    .setPackageTool(packageTool)
                    .setTargetStaticDir(targetStaticDir)
                    .execute()
                    .getResult()
            if (this.baseImage == 'webstatic') {
                hasFilesJson = true
            }
        } else {
            this.context.jenkins.error "不支持的代码编译类型"
        }
        this.context.jenkins.echo "打包完成: targetDir=${targetDir}"
        this.context.jenkins.sh "cd ${targetDir} && ls -l"


        // 制作镜像
        String image = new DockerCmd(context)
                .setCodeType(this.codeType)
                .setTargetDir(targetDir)
                .setRegistry(this.registry)
                .setImagePath(this.imagePath)
                .setRepository(this.projectName)
                .setTag("${this.context.branchName}.${version}")
                .setBaseImage(this.baseImage)
                .setHasFilesJson(hasFilesJson)
                .execute().getResult()
        this.context.jenkins.echo "制作镜像完成: image=${image}"

        this.resultImpl = image
        return this
    }

    @Override
    String getResult() {
        return resultImpl
    }

    String getCodeType() {
        return codeType
    }

    BuildAppImageCmd setCodeType(String codeType) {
        this.codeType = codeType
        return this
    }

    String getBuildDir() {
        return buildDir
    }

    BuildAppImageCmd setBuildDir(String buildDir) {
        this.buildDir = buildDir
        return this
    }

    String getProjectName() {
        return projectName
    }

    BuildAppImageCmd setProjectName(String projectName) {
        this.projectName = projectName
        return this
    }

    String getBaseImage() {
        return baseImage
    }

    BuildAppImageCmd setBaseImage(String baseImage) {
        this.baseImage = baseImage
        return this
    }

    String getResultImpl() {
        return resultImpl
    }

    String getRegistry() {
        return registry
    }

    BuildAppImageCmd setRegistry(String registry) {
        this.registry = registry
        return this
    }

    String getImagePath() {
        return imagePath
    }

    BuildAppImageCmd setImagePath(String imagePath) {
        this.imagePath = imagePath
        return this
    }

    String getPackageTool() {
        return packageTool
    }

    BuildAppImageCmd setPackageTool(String packageTool) {
        this.packageTool = packageTool
        return this
    }

    String getTargetStaticDir() {
        return targetStaticDir
    }

    BuildAppImageCmd setTargetStaticDir(String targetStaticDir) {
        this.targetStaticDir = targetStaticDir
        return this
    }
}
