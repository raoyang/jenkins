package cmd

import entity.Context

/**
 *  maven打包
 */
class MvnPackageCmd extends AbstractCmd<String> {

    private String buildDir

    MvnPackageCmd(Context context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("buildDir", this.buildDir)

        this.context.jenkins.sh "cd ${this.buildDir} && mvn clean && mvn package"

        return this
    }

    @Override
    String getResult() {
        return "${this.buildDir}/target"
    }

    String getBuildDir() {
        return buildDir
    }

    MvnPackageCmd setBuildDir(String buildDir) {
        this.buildDir = buildDir
        return this
    }
}
