package pipeline.lateinos

import cmd.GetAllBranchesCmd
import cmd.InitGitCmd
import cmd.MergeCodeCmd
import entity.Context
import pipeline.BaseProcess

/**
 *  主干分支流程
 */
class MainProcess extends BaseProcess {

    MainProcess(Context context) {
        super(context)
    }

    @Override
    def careProjects() {
        return null
    }

    @Override
    void execute() {
        finishedNotifyCmd.notifyMsg["功能"] = "合并分支"

        // 合并到特定分支
        mergeToBranch("test")
        mergeToBranch("sdk-publish")
        mergeToBranch("pre-publish")
        mergeToBranch("publish")

        // 合并至所有开发类型分支
        this.mergeToDev("main")
    }

    void mergeToBranch(String targetBranchName) {
        this.processMerge("main", targetBranchName)
    }

}
