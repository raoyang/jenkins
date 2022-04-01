package cmd

import entity.Context

/**
 *  Webapp类型项目
 */
class WebappPackageCmd extends AbstractCmd<String>{

    private String buildDir
    private String version
    private String packageTool

    private String targetStaticDir = "dist"

    WebappPackageCmd(Context context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("buildDir", this.buildDir)
        checkEmpty("version", this.version)
        checkEmpty("packageTool", this.packageTool)

        this.context.jenkins.dir(this.buildDir) {
            this.context.jenkins.sh "rm -rf node_modules"
            this.context.jenkins.sh "rm -rf ${this.targetStaticDir}"
            installPackages()
            buildDist()
        }

        return this
    }

    private void installPackages() {
        if (packageTool == 'yarn') {
            this.context.jenkins.sh "yarn install"
        } else if (packageTool == 'npm') {
            this.context.jenkins.sh "npm install"
        } else if (packageTool == 'pnpm') {
            this.context.jenkins.sh "pnpm install:packages"
        } else {
            this.context.jenkins.error "unsupported package tool"
        }
    }

    private void buildDist() {
        if (packageTool == 'yarn') {
            this.context.jenkins.sh "yarn build"
        } else if (packageTool == 'npm') {
            this.context.jenkins.sh "npm run build"
        } else if (packageTool == 'pnpm') {
            this.context.jenkins.sh "pnpm build"
        }
    }

    @Override
    String getResult() {
        return "${this.buildDir}/${this.targetStaticDir}"
    }

    String getBuildDir() {
        return buildDir
    }

    WebappPackageCmd setBuildDir(String buildDir) {
        this.buildDir = buildDir
        return this
    }

    String getVersion() {
        return version
    }

    WebappPackageCmd setVersion(String version) {
        this.version = version
        return this
    }

    String getPackageTool() {
        return packageTool
    }

    WebappPackageCmd setPackageTool(String packageTool) {
        this.packageTool = packageTool
        return this
    }

    String getTargetStaticDir() {
        return targetStaticDir
    }

    WebappPackageCmd setTargetStaticDir(String targetStaticDir) {
        if (targetStaticDir == null || targetStaticDir.isEmpty()) {
            return this
        }

        this.targetStaticDir = targetStaticDir
        return this
    }
}
