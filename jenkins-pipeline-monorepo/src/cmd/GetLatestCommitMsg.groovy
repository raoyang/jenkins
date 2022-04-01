package cmd

import entity.LatestCommitMsg

class GetLatestCommitMsg extends AbstractCmd<LatestCommitMsg> {

    private LatestCommitMsg latestCommitMsg

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2

    GetLatestCommitMsg(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        LatestCommitMsg msg = null
        if (this.retryTimes > 0) {
            this.context.jenkins.retry(this.retryTimes + 1) {
                msg = this.buildMsg()
            }
        } else {
            msg = this.buildMsg()
        }

        this.latestCommitMsg = msg
        return this
    }

    private LatestCommitMsg buildMsg() {
        LatestCommitMsg msg = new LatestCommitMsg()
        String committerEmail = this.context.jenkins.sh(script: "git show -s --format=%ce", returnStdout: true).trim()
        this.context.jenkins.echo "${committerEmail}"
        String committerName = committerEmail.replace("@authine.com","").trim()
        String authorEmail = this.context.jenkins.sh(script: "git show -s --format=%ae", returnStdout: true).trim()
        this.context.jenkins.echo "${authorEmail}"
        String authorName = authorEmail.replace("@authine.com","").trim()
        String commitHash = this.context.jenkins.sh(script: "git show -s --format=%H", returnStdout: true).trim()
        String shortCommitHash = this.context.jenkins.sh(script: "git show -s --format=%h", returnStdout: true).trim()
        String committerDate = this.context.jenkins.sh(script: "git show -s --format=%cd", returnStdout: true).trim()
        String subject = this.context.jenkins.sh(script: "git show -s --format=%s", returnStdout: true).trim()
        String fullMsg = this.context.jenkins.sh(script: "git show -s", returnStdout: true).trim()
        msg.setCommitterName(committerName)
                .setCommitterEmail(committerEmail)
                .setAuthorName(authorName)
                .setAuthorEmail(authorEmail)
                .setCommitHash(commitHash)
                .setShortCommitHash(shortCommitHash)
                .setCommitterDate(committerDate)
                .setSubject(subject)
                .setFullMsg(fullMsg)

        return msg
    }

    @Override
    LatestCommitMsg getResult() {
        return this.latestCommitMsg
    }

    int getRetryTimes() {
        return retryTimes
    }

    GetLatestCommitMsg setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes
        return this
    }
}
