package cmd

import entity.Context

/**
 * java打包命令
 */
class JavaPackageCmd extends AbstractCmd<String> {

    private String buildDir
    private String version

    private boolean needBuild
    private boolean needBootJar
    private boolean needPush

    JavaPackageCmd(Context context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("buildDir", this.buildDir)
        checkEmpty("version", this.version)

        this.context.jenkins.sh "cd ${this.buildDir} && gradle clean"

        if (needBuild) {
            String cmd = "cd ${this.buildDir} && gradle build"
            if (this.version) {
                cmd += " -Dgradle.sdk.target.version=${this.version}"
            }
            this.context.jenkins.sh "${cmd}"
        }

        if (needBootJar) {
            this.context.jenkins.sh "cd ${this.buildDir} && gradle bootJar"
        }

        if (needPush) {
            String cmd = "cd ${this.buildDir} && gradle publish"
            if (this.version) {
                cmd += " -Dgradle.sdk.target.version=${this.version}"
            }
            this.context.jenkins.retry(3) {
                this.context.jenkins.sh "${cmd}"
            }
        }

        return this
    }

    @Override
    String getResult() {
        return "${this.buildDir}/build/libs"
    }

    String getBuildDir() {
        return buildDir
    }

    JavaPackageCmd setBuildDir(String buildDir) {
        this.buildDir = buildDir
        return this
    }

    String getVersion() {
        return version
    }

    JavaPackageCmd setVersion(String version) {
        this.version = version
        return this
    }

    boolean getNeedBuild() {
        return needBuild
    }

    JavaPackageCmd setNeedBuild(boolean needBuild) {
        this.needBuild = needBuild
        return this
    }

    boolean getNeedBootJar() {
        return needBootJar
    }

    JavaPackageCmd setNeedBootJar(boolean needBootJar) {
        this.needBootJar = needBootJar
        return this
    }

    boolean getNeedPush() {
        return needPush
    }

    JavaPackageCmd setNeedPush(boolean needPush) {
        this.needPush = needPush
        return this
    }
}
