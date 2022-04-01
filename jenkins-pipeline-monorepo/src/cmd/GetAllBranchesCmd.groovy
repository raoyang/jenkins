package cmd

class GetAllBranchesCmd extends AbstractCmd<List<String>>{

    private List<String> branchList

    /**
     * 请求失败重试次数
     */
    private int retryTimes = 2

    GetAllBranchesCmd(context) {
        super(context)
    }

    @Override
    AbstractCmd execute() {
        List<String> resultList = []
        String branchText = ""
        if (this.retryTimes > 0) {
            this.context.jenkins.retry(this.retryTimes + 1) {
                branchText = this.context.jenkins.sh(script: "git branch -r",returnStdout: true).trim()
            }
        } else {
            branchText = this.context.jenkins.sh(script: "git branch -r",returnStdout: true).trim()
        }
        List originBranch = branchText.split("\n")
        for(String branch : originBranch){
            resultList.add(branch.replace("origin/","").trim())
        }
        this.branchList = resultList
        return this
    }

    @Override
    List<String> getResult() {
        return this.branchList
    }

    int getRetryTimes() {
        return retryTimes
    }

    GetAllBranchesCmd setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes
        return this
    }
}
