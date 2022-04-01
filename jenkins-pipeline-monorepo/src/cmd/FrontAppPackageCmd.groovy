package cmd

import entity.ProjectItemConfig

/**
 * subapp 类型项目打包
 */
class FrontAppPackageCmd extends AbstractCmd<Void> {

    private ProjectItemConfig projectItemConfig
    private String distDir

    FrontAppPackageCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        String assetsAppName = build();
        copyDist(assetsAppName)
        return this
    }

    @Override
    Void getResult() {
        return null
    }

    private String build() {
        String buildDir = this.projectItemConfig.getBuildDir()
        String assetsAppName
        this.context.jenkins.dir("${buildDir}") {
            this.context.jenkins.sh """
                pnpm ci
            """
            try {
                def json = this.context.jenkins.readJSON file: "package.json"
                assetsAppName = json["eros"]["assetsPublicPath"]
            } catch (Exception e) {
                this.context.jenkins.error "eros['assetsPublicPath'] not defined"
            }
        }
        return assetsAppName
    }

    private void copyDist(String assetsAppName) {
        String buildDir = this.projectItemConfig.getBuildDir()
        this.context.jenkins.echo "${assetsAppName}"
        this.context.jenkins.dir("${buildDir}") {
            this.context.jenkins.sh """
                mkdir -p ${distDir}/${assetsAppName} 
            """
        }
        this.context.jenkins.dir("${buildDir}") {
            this.context.jenkins.sh """
                cp -rf dist/* ${distDir}/${assetsAppName}
            """
        }
    }

    FrontAppPackageCmd setProjectItemConfig(ProjectItemConfig projectItemConfig) {
        this.projectItemConfig = projectItemConfig
        return this
    }
    
    FrontAppPackageCmd setDistDir(String distDir) {
        this.distDir = distDir
        return this
    }

}
