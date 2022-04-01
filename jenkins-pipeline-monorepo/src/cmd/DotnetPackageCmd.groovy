package cmd

import entity.Context

/**
 * dotnet打包命令
 */
class DotnetPackageCmd extends AbstractCmd<String> {

    private String buildDir
    private String version
    private boolean needPush

    DotnetPackageCmd(Context context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("buildDir", this.buildDir)
        checkEmpty("version", this.version)

        String cmd = "cd ${this.buildDir} && rm -rf bin && dotnet pack -p:PackageVersion=${this.version} -c Release && cd bin/Release"
        if (needPush) {
            cmd += " && dotnet nuget push *.nupkg -k 1a06ffa9-6ede-3934-84a0-32dab496157d -s H3"
        }

        this.context.jenkins.retry(2) {
            this.context.jenkins.sh "${cmd}"
        }

        return this
    }

    @Override
    String getResult() {
        return "${this.buildDir}"
    }

    String getBuildDir() {
        return buildDir
    }

    DotnetPackageCmd setBuildDir(String buildDir) {
        this.buildDir = buildDir
        return this
    }

    String getVersion() {
        return this.version
    }

    DotnetPackageCmd setVersion(String version) {
        this.version = version
        return this
    }

    boolean isNeedPush() {
        return this.needPush
    }

    DotnetPackageCmd setNeedPush(boolean needPush) {
        this.needPush = needPush
        return this
    }

}
