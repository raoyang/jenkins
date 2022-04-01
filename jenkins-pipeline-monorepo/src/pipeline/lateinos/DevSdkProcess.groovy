package pipeline.lateinos

import cmd.DotnetPackageCmd
import cmd.JavaPackageCmd
import entity.Context
import entity.ProjectItemConfig
import pipeline.BaseProcess

/**
 *  SDK开发检查流程
 */
class DevSdkProcess extends BaseProcess {

    private ProjectItemConfig javaAllPackConfig
    private ProjectItemConfig javaPagingJdbcConfig
    private ProjectItemConfig dotnetAllPackConfig

    DevSdkProcess(Context context) {
        super(context)
    }

    @Override
    def careProjects() {
        return [
                LateinosProjectNames.ALLPACK_JAVA,
                LateinosProjectNames.PAGING_JDBC_JAVA,
                LateinosProjectNames.ALLPACK_DOTNET
        ]
    }

    @Override
    void execute() {
        javaAllPackConfig = context.getProjectsConfig().get(LateinosProjectNames.ALLPACK_JAVA)
        javaPagingJdbcConfig = context.getProjectsConfig().get(LateinosProjectNames.PAGING_JDBC_JAVA)
        dotnetAllPackConfig = context.getProjectsConfig().get(LateinosProjectNames.ALLPACK_DOTNET)

        finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        tryBuild()
    }

    private void tryBuild() {
        this.context.jenkins.stage('SDK编译测试') {
            if (javaAllPackConfig.needBuild) {
                finishedNotifyCmd.addToNotifyUserList(javaAllPackConfig.projectMaintainer)

                context.jenkins.echo "编译Lateinos SDK AllPack的Java版"

                this.sonarCheck(javaAllPackConfig)

                JavaPackageCmd packageCmd = new JavaPackageCmd(this.context)
                        .setBuildDir(javaAllPackConfig.buildDir)
                        .setVersion("0.0.0-SNAPSHOT")
                        .setNeedBuild(true)
                        .setNeedPush(false)

                packageCmd.execute()
            }

            if (javaPagingJdbcConfig.needBuild) {
                finishedNotifyCmd.addToNotifyUserList(javaPagingJdbcConfig.projectMaintainer)

                context.jenkins.echo "编译Lateinos SDK PagingJdbc的Java版"

                this.sonarCheck(javaPagingJdbcConfig)

                JavaPackageCmd packageCmd = new JavaPackageCmd(this.context)
                        .setBuildDir(javaPagingJdbcConfig.buildDir)
                        .setVersion("0.0.0-SNAPSHOT")
                        .setNeedBuild(true)
                        .setNeedPush(false)

                packageCmd.execute()
            }

            if (dotnetAllPackConfig.needBuild) {
                finishedNotifyCmd.addToNotifyUserList(dotnetAllPackConfig.projectMaintainer)

                this.context.jenkins.echo "编译Lateinos SDK AllPack的Dotnet版本"

                this.sonarCheck(dotnetAllPackConfig)

                DotnetPackageCmd packageCmd = new DotnetPackageCmd(this.context)
                        .setBuildDir(dotnetAllPackConfig.buildDir)
                        .setVersion("0.0.0")
                        .setNeedPush(false)

                packageCmd.execute()
            }
        }
    }
}
