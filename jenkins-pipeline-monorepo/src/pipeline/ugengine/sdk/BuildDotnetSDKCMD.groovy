package pipeline.ugengine.sdk

import cmd.AbstractCmd
import cmd.DotnetPackageCmd
import entity.Context
import entity.ProjectItemConfig

import java.text.SimpleDateFormat

class BuildDotnetSDKCMD extends AbstractCmd<Object> {
    BuildDotnetSDKCMD(Context context){
        super(context)
    }

    ProjectItemConfig getConfig_sdk() {
        return config_sdk
    }

    BuildDotnetSDKCMD setConfig_sdk(ProjectItemConfig config_sdk) {
        this.config_sdk = config_sdk
        return this
    }
    ProjectItemConfig config_sdk

    boolean getNeedPush() {
        return needPush
    }

    BuildDotnetSDKCMD setNeedPush(boolean needPush) {
        this.needPush = needPush
        return this
    }
    boolean needPush

    @Override
    AbstractCmd execute() {
        checkEmpty("config_sdk", this.config_sdk)
        buildDotnetSdk(this.config_sdk)
    }

    @Override
    Object getResult() {
        return null
    }

    private void buildDotnetSdk(ProjectItemConfig config_engine_sdk){
        if(!config_engine_sdk.needBuild){
            return
        }
        //finishedNotifyCmd.addToNotifyUserList(config_engine_sdk.projectMaintainer)
        String workspace = this.context.jenkins.env.WORKSPACE
        if(config_engine_sdk.codeType != "dotnet") {
            this.context.jenkins.echo "${config_engine_sdk.projectName} 不是dotnet项目,不能构建dotnet包 "
            return
        }
        // 1.获取变更的文件集合
        String[] changeFiles = getChangedFiles()

        // 2.查找所有的项目文件  find /home/jenkins/agent/workspace/sdk/dotnet-h3yun-engine-infrastructure -name '*.csproj'
        String[] findFiles = getAllProjectFiles(config_engine_sdk, workspace)

        // 3.查找需要构建的项目目录
        String[] buildPaths = getProjectFilePaths(findFiles, changeFiles)

        // 4.打包
        pack(buildPaths)

    }

    private String[] getChangedFiles() {
        this.context.jenkins.echo  "git show --pretty='format:' --name-only"
        String gitChanged = this.context.jenkins.sh(script:"git show --pretty='format:' --name-only", returnStdout:true).trim()
        this.context.jenkins.echo "${gitChanged}"
        String[] changedFiles = []
        if(gitChanged != null) {
            changedFiles = gitChanged.split(/[\s\n]/)
        }
        return changedFiles
    }

    private String[] getAllProjectFiles(ProjectItemConfig config_sdk, String workspace) {
        this.context.jenkins.echo  "find ${workspace}/${config_sdk.codeDir} -name '*.csproj'"
        String findFile = context.jenkins.sh(script:"find ${workspace}/${config_sdk.codeDir} -name '*.csproj'", returnStdout:true).trim()
        this.context.jenkins.echo  "find result ${findFile}"
        if(findFile == null){
            return []
        }
        String[] findFiles = findFile.split(/[\s\n]/)
        return findFiles
    }

    private String[] getProjectFilePaths(String[] projectFiles, String[] changeFiles) {
        def buildPaths = []
        String workspace = this.context.jenkins.env.WORKSPACE
        if(projectFiles == null || projectFiles.length == 0){
            return buildPaths
        }
        //String projectFileExt = "*.csproj"
        for (String projectFileFullPath : projectFiles) {
            this.context.jenkins.echo "组装csproj文件路径: projectFilePath=${projectFileFullPath}"

            String projectFileName = projectFileFullPath.find("[\\w\\.]+.csproj")
            this.context.jenkins.echo "projectFileName: ${projectFileName}"
            String projectPath = projectFileFullPath.replace("${projectFileName}", "")
                                                            .replace("${workspace}/", "")
            this.context.jenkins.echo "tobeBuildProjectPath is: ${projectPath}"
            for (String changeFile : changeFiles) {
                if (projectPath == "" || projectPath == "-") {
                    continue
                }
                String toBuildProjectPath = changeFile.find("${projectPath}")
                this.context.jenkins.echo "csproj文件相对路径: ${toBuildProjectPath}"
                //排除测试项目
                String test = projectPath.find("Test")
                if(toBuildProjectPath != null && test == null) {
                    buildPaths.push(projectPath)
                }
            }
        }
        return buildPaths.unique()
    }

    private void pack(String[] buildPaths){
        if(buildPaths == null){
            return
        }
        String workspace = this.context.jenkins.env.WORKSPACE
        for (String buildPath : buildPaths) {
            try {
                String finalBuildDir= "${workspace}/${buildPath}"
                String version = new SimpleDateFormat("yyyy.MMddHHmm.")
                        .format(new Date(this.context.jenkins.currentBuild.startTimeInMillis))
                version += this.context.jenkins.env.BUILD_NUMBER
                String packageDir = new DotnetPackageCmd(context)
                        .setBuildDir(finalBuildDir)
                        .setVersion(version)
                        .setNeedPush(this.needPush)
                        .execute()
                        .getResult()
                this.context.jenkins.echo "打包完成: packageDir=${packageDir}"
            } catch (Exception e) {
                throw new Exception("${buildPath}打包失败，errorMsg: ${e.getMessage()}")
            }
        }
    }
}
