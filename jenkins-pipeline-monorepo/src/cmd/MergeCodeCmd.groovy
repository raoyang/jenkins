package cmd

import entity.Context

class MergeCodeCmd extends AbstractCmd<Void> {

    private String sourceBranch
    private String targetBranch

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2

    /**
     * 合并分支
     * @param sourceBranch 合并的源分支
     * @param targetBranch 合并的目标分支
     */
    MergeCodeCmd(Context context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        checkEmpty("sourceBranch", this.sourceBranch)
        checkEmpty("targetBranch", this.targetBranch)

        if (this.retryTimes > 0) {
            this.context.jenkins.retry(this.retryTimes + 1) {
                this.gitMerge()
            }
        } else {
            this.gitMerge()
        }

        return this
    }

    private void gitMerge() {
        this.context.jenkins.sh """
                git fetch
                git checkout "${this.targetBranch}"
                git pull origin "${this.targetBranch}"
                git merge "${this.sourceBranch}"
                git push origin "${this.targetBranch}"
            """
    }

    String getSourceBranch() {
        return sourceBranch
    }

    MergeCodeCmd setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch
        return this
    }

    String getTargetBranch() {
        return targetBranch
    }

    MergeCodeCmd setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch
        return this
    }

    int getRetryTimes() {
        return retryTimes
    }

    MergeCodeCmd setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes
        return this
    }

    @Override
    Void getResult() {
        return null
    }
}
