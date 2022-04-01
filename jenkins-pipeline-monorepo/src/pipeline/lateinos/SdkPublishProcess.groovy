package pipeline.lateinos

import cmd.DotnetPackageCmd
import cmd.InitGitCmd
import cmd.JavaPackageCmd
import cmd.MergeCodeCmd
import entity.Context
import entity.ProjectItemConfig
import pipeline.BaseProcess

import java.text.SimpleDateFormat

/**
 * 开天SDK的发布流程
 * @author wangli
 */
class SdkPublishProcess extends BaseProcess {

    private ProjectItemConfig javaAllPackConfig
    private ProjectItemConfig javaPagingJdbcConfig
    private ProjectItemConfig dotnetAllPackConfig

    SdkPublishProcess(Context context) {
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
        this.finishedNotifyCmd.setNotifyToGlobalFlag(true)
        this.javaAllPackConfig = context.getProjectsConfig().get(LateinosProjectNames.ALLPACK_JAVA)
        this.javaPagingJdbcConfig = context.getProjectsConfig().get(LateinosProjectNames.PAGING_JDBC_JAVA)
        this.dotnetAllPackConfig = context.getProjectsConfig().get(LateinosProjectNames.ALLPACK_DOTNET)

        new InitGitCmd(this.context).execute()
        buildAndPublish()
        mergeToMainBranch()
    }

    void buildAndPublish() {
        String version = new SimpleDateFormat("yyyy.MMddHHmm.")
                .format(new Date(this.context.jenkins.currentBuild.startTimeInMillis))
        version += this.context.jenkins.env.BUILD_NUMBER

        this.context.jenkins.stage('SDK编译&发布') {
            if (javaAllPackConfig.needBuild) {
                context.jenkins.echo "编译Lateinos SDK AllPack的Java版, 版本为${version}"

                finishedNotifyCmd.notifyMsg["${javaAllPackConfig.projectName}版本"] = "${version}"
                JavaPackageCmd packageCmd = new JavaPackageCmd(this.context)
                            .setBuildDir(javaAllPackConfig.buildDir)
                            .setVersion(version)
                            .setNeedBuild(true)
                            .setNeedPush(true)

                packageCmd.execute()
            }

            if (javaPagingJdbcConfig.needBuild) {
                context.jenkins.echo "编译Lateinos SDK PagingJdbc的Java版, 版本为${version}"

                finishedNotifyCmd.notifyMsg["${javaPagingJdbcConfig.projectName}版本"] = "${version}"
                JavaPackageCmd packageCmd = new JavaPackageCmd(this.context)
                        .setBuildDir(javaPagingJdbcConfig.buildDir)
                        .setVersion(version)
                        .setNeedBuild(true)
                        .setNeedPush(true)

                packageCmd.execute()
            }

            if (dotnetAllPackConfig.needBuild) {
                this.context.jenkins.echo "编译Lateinos SDK AllPack的Dotnet版本， 版本为${version}"

                finishedNotifyCmd.notifyMsg["${dotnetAllPackConfig.projectName}版本"] = "${version}"
                DotnetPackageCmd packageCmd = new DotnetPackageCmd(this.context)
                            .setBuildDir(dotnetAllPackConfig.buildDir)
                            .setVersion(version)
                            .setNeedPush(true)

                packageCmd.execute()
            }
        }

    }

    void mergeToMainBranch() {
        this.context.jenkins.stage('合并SDK-PUBLISH分支至主干') {
            new MergeCodeCmd(this.context)
                    .setSourceBranch("sdk-publish")
                    .setTargetBranch("main")
                    .execute()
        }
    }

}
