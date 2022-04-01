package cmd

import entity.Context

class InitGitCmd extends AbstractCmd<Void>{

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2

    InitGitCmd(Context context){
        super(context)
    }

    @Override
    AbstractCmd execute() {
        //使git缓存用户凭证
        if (this.retryTimes > 0) {
            this.context.jenkins.retry(this.retryTimes + 1) {
                this.gitConfig()
            }
        } else {
            this.gitConfig()
        }
        return this
    }

    private void gitConfig(){
        this.context.jenkins.sh "git config --global credential.helper store"
        this.context.jenkins.git(branch: "${this.context.getBranchName()}", credentialsId: "${this.context.jenkins.params.gitMergeUserId}", url:"${this.context.jenkins.env.GIT_URL}")
        this.context.jenkins.sh "git config --global user.email ugmerge01@authine.com"
        this.context.jenkins.sh "git config --global user.name ugmerge01"
    }

    @Override
    Void getResult() {
        return null
    }

    int getRetryTimes() {
        return retryTimes
    }

    InitGitCmd setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes
        return this
    }
}
