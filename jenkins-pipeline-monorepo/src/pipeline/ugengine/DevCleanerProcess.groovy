package pipeline.ugengine

import entity.Context
import pipeline.BaseProcess

/**
 *  cleaner开发流程流水线
 */
class DevCleanerProcess extends BaseProcess {
    DevCleanerProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return []
    }

    @Override
    protected void execute() {
        this.finishedNotifyCmd.addToNotifyUserList(this.latestCommitMsg.committerName)

        this.context.jenkins.stage("配置检查") {

        }

        this.context.jenkins.stage("代码扫描") {

        }
    }
}
