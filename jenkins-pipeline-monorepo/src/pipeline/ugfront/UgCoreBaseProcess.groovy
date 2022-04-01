package pipeline.ugfront

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import cmd.FrontAppPackageCmd
import cmd.FrontLcosKernelPackageCmd
import cmd.FrontPolymerPackageCmd
import cmd.OssUploadCmd
import entity.Context
import entity.ProjectItemConfig
import pipeline.BaseProcess

abstract class UgCoreBaseProcess extends BaseProcess {

    UgCoreBaseProcess(Context context) {
        super(context)
    }

    @Override
    def careProjects() {
        def projects = new ArrayList();
        projects.add(ProjectNames.LCOS_KERNEL);
        projects.add(ProjectNames.PC_POLYMER);
        projects.addAll(ProjectNames.PC_APPS);
        projects.add(ProjectNames.MOBILE_POLYMER);
        projects.addAll(ProjectNames.MOBILE_APPS);
        projects.add(ProjectNames.EXTERNAL_POLYMER);
        projects.addAll(ProjectNames.EXTERNAL_APPS);
        return projects;
    }

    @Override
    void execute() {
        Map<String, ProjectItemConfig> projectsConfig = this.context.projectsConfig;
        ProjectItemConfig pcPloymerConfig = projectsConfig.get(ProjectNames.PC_POLYMER);
        ProjectItemConfig mobilePloymerConfig = projectsConfig.get(ProjectNames.MOBILE_POLYMER);
        ProjectItemConfig externalPloymerConfig = projectsConfig.get(ProjectNames.EXTERNAL_POLYMER);
        String buildVersion = this.createVersion();
        this.context.jenkins.stage("kernerl打包") {
            ProjectItemConfig lcosKernelConfig = projectsConfig.get(ProjectNames.LCOS_KERNEL);
            // kernel自身、PC端、移动端、外链 变更都要触发kernel打包
            if (lcosKernelConfig.needBuild
                    || pcPloymerConfig.needBuild
                    || mobilePloymerConfig.needBuild
                    || externalPloymerConfig.needBuild) {
                new FrontLcosKernelPackageCmd(this.context).setProjectItemConfig(lcosKernelConfig).execute();
            }
        }

        this.context.jenkins.stage("pc打包部署") {
            if (pcPloymerConfig.needBuild) {
                runAppPackage(projectsConfig ,ProjectNames.PC_APPS);
                new FrontPolymerPackageCmd(this.context)
                        .setProjectItemConfig(pcPloymerConfig)
                        .setBuildVersion(buildVersion)
                        .execute();
                new OssUploadCmd(this.context)
                        .setOssEndpoint("oss-cn-hangzhou.aliyuncs.com")
                        .setResourcesUpload("oss://youge-webstatic")
                        .setCredentialId(this.context.jenkins.params.ossCredentialId)
                        .setIsRecursive(true)
                        .setFileDir("${pcPloymerConfig.buildDir}/dist")
                        .setCloudDir(getCloudDir("pc", buildVersion))
                        .execute();
            }
        }

        this.context.jenkins.stage("mobile打包部署") {
            if (mobilePloymerConfig.needBuild) {
                runAppPackage(projectsConfig ,ProjectNames.MOBILE_APPS);
                new FrontPolymerPackageCmd(this.context)
                        .setProjectItemConfig(mobilePloymerConfig)
                        .setBuildVersion(buildVersion)
                        .execute();
                new OssUploadCmd(this.context)
                        .setOssEndpoint("oss-cn-hangzhou.aliyuncs.com")
                        .setResourcesUpload("oss://youge-webstatic")
                        .setCredentialId(this.context.jenkins.params.ossCredentialId)
                        .setIsRecursive(true)
                        .setFileDir("${mobilePloymerConfig.buildDir}/dist")
                        .setCloudDir(getCloudDir("mobile", buildVersion))
                        .execute();
            }
        }
        this.context.jenkins.stage("外链打包部署") {
            if (externalPloymerConfig.needBuild) {
                runAppPackage(projectsConfig ,ProjectNames.EXTERNAL_APPS);
                new FrontPolymerPackageCmd(this.context)
                        .setProjectItemConfig(externalPloymerConfig)
                        .setBuildVersion(buildVersion)
                        .execute();
                new OssUploadCmd(this.context)
                        .setOssEndpoint("oss-cn-hangzhou.aliyuncs.com")
                        .setResourcesUpload("oss://youge-webstatic")
                        .setCredentialId(this.context.jenkins.params.ossCredentialId)
                        .setIsRecursive(true)
                        .setFileDir("${externalPloymerConfig.buildDir}/dist")
                        .setCloudDir(getCloudDir("external", buildVersion))
                        .execute();
            }
        }

        doVerification();
    }

    protected abstract String getCloudDir(String module, String version);
    
    protected abstract void doVerification();

    protected String createVersion() {
        String gitCommit = this.context.jenkins.env.GIT_COMMIT
        String shortHash = this.context.jenkins.sh(script: "git rev-parse --short ${gitCommit}", returnStdout: true).trim()
        return "${shortHash}-" + new Date().format('yyMMddHHmm') + "-${this.context.jenkins.env.BUILD_ID}"
    }

    protected void runAppPackage(Map<String, ProjectItemConfig> projectsConfig, String[] projectNames) {
        for(String projectName: projectNames) {
            ProjectItemConfig config = projectsConfig.get(projectName);
            this.context.jenkins.echo "run task: ${projectName}";
            new FrontAppPackageCmd(this.context)
                    .setProjectItemConfig(config)
                    .setDistDir("../dist")
                    .execute();
        }
    }
}
