package cmd

import entity.Context

/**
 * 生成部署模版
 */
class GenerateDeployYamls extends AbstractCmd<List<String>> {

    private String targetDir  // 生成文件的目标目录
    private String codeType   // 部署文件代码类型
    private String deployBaseTemplate // 部署基础模版

    private String deployName   // 部署项目名
    private String deployImage  // 部署镜像
    private String deployNamespace // 部署命名控件
    private String deployReplicas  // 部署副本数

    private List<String> targetFileNames // 目标文件名列表
    private Map variables // 动态变量

    GenerateDeployYamls(Context context) {
        super(context)
    }

    String renderTemplate(String template)
            throws IOException, ClassNotFoundException {
        String result = template.replace("#deployName#", this.deployName)
        result = result.replace("#deployImage#", this.deployImage)
        result = result.replace("#deployNamespace#", this.deployNamespace)
        result = result.replace("#deployReplicas#", this.deployReplicas)
        result = result.replace("#codeType#", this.codeType)
        result = result.replace("#deployBaseTemplate#", this.deployBaseTemplate)

        if (variables != null) {
            for (variable in variables) {
                result = result.replace("#${variable.key.toString()}#", variable.value.toString())
            }
        }
        return result
    }

    private def parseFileItems(String templateDir) {
        String filesJsonTemplate = this.context.jenkins.libraryResource "${templateDir}/files.json"
        String filesJsonContent = renderTemplate(filesJsonTemplate)
        this.context.jenkins.echo "${filesJsonContent}"
        def filesJson = this.context.jenkins.readJSON text:filesJsonContent
        this.context.jenkins.echo "${filesJson}"
        return filesJson.files
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("targetDir", this.targetDir)
        checkEmpty("codeType", this.codeType)
        checkEmpty("deployBaseTemplate", this.deployBaseTemplate)
        checkEmpty("deployName", this.deployName)
        checkEmpty("deployNamespace", this.deployNamespace)
        checkEmpty("deployReplicas", this.deployReplicas)

        String templateDir = "deployTemplates/${this.codeType}/${this.deployBaseTemplate}"

        def fileItems = parseFileItems(templateDir)
        this.targetFileNames = new ArrayList<>()
        for (def fileItem : fileItems) {
            String sourceFileName = fileItem.source
            String targetFileName = fileItem.target

            this.context.jenkins.echo "generate file from ${templateDir}/${sourceFileName} to ${targetDir}/${targetFileName}"

            String sourceFileContent = this.context.jenkins.libraryResource "${templateDir}/${sourceFileName}"
            String targetFileContent = renderTemplate(sourceFileContent)
            this.context.jenkins.echo "${targetFileContent}"

            this.context.jenkins.dir(targetDir) {
                this.context.jenkins.writeFile file: "./${targetFileName}", text: targetFileContent
            }

            this.targetFileNames.add(targetFileName)
            this.context.jenkins.sh "ls -al ${targetDir}"
        }

        return this
    }

    @Override
    List<String> getResult() {
        return this.targetFileNames
    }

    String getTargetDir() {
        return targetDir
    }

    GenerateDeployYamls setTargetDir(String targetDir) {
        this.targetDir = targetDir
        return this
    }

    String getCodeType() {
        return codeType
    }

    GenerateDeployYamls setCodeType(String codeType) {
        this.codeType = codeType
        return this
    }

    String getDeployName() {
        return deployName
    }

    GenerateDeployYamls setDeployName(String deployName) {
        this.deployName = deployName
        return this
    }

    String getDeployImage() {
        return deployImage
    }

    GenerateDeployYamls setDeployImage(String deployImage) {
        this.deployImage = deployImage
        return this
    }

    String getDeployNamespace() {
        return deployNamespace
    }

    GenerateDeployYamls setDeployNamespace(String deployNamespace) {
        this.deployNamespace = deployNamespace
        return this
    }

    String getDeployReplicas() {
        return deployReplicas
    }

    GenerateDeployYamls setDeployReplicas(String deployReplicas) {
        this.deployReplicas = deployReplicas
        return this
    }

    String getDeployBaseTemplate() {
        return deployBaseTemplate
    }

    GenerateDeployYamls setDeployBaseTemplate(String deployBaseTemplate) {
        this.deployBaseTemplate = deployBaseTemplate
        return this
    }

    Map getVariables() {
        return variables
    }

    GenerateDeployYamls setVariables(Map variables) {
        this.variables = variables
        return this
    }

}
