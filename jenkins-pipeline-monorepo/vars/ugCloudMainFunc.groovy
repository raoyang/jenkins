import cmd.ContextInfoCmd
import entity.Context
import pipeline.BaseProcess
import pipeline.GitlabWebHookHelper
import pipeline.ugcloud.DevAutotestProcess
import pipeline.ugcloud.DevGatewayProcess

static def createProcess(Context context, String branchName) {
    switch (branchName) {
        case "dev":
            return new DevGatewayProcess(context)
        case "dev-autotest":
            return new DevAutotestProcess(context)
    }
    return null
}

def getBranchName() {
    String branchName = params.buildBranch
    if (branchName == null || branchName.isEmpty()) {
        branchName = env.BRANCH_NAME
    }
    echo("当前构建分支: ${branchName}")
    return branchName
}

def call() {
    echo "---------- 构建开始 ----------"

    String workspace = env.WORKSPACE
    echo "工作空间目录: ${workspace}"

    String branchName = getBranchName()
    if (branchName == null || branchName.isEmpty()) {
        // 无分支
        echo("流水线无明确分支名称，终止流水线执行!!!")
        return
    }

    sh("ls -l")

    echo sh(returnStdout: true, script: 'env')

    // 解析配置
    Context context = new ContextInfoCmd(this)
            .setFilePath("UgCloudProjectConfig.yaml")
            .setBranchName(branchName)
            .execute()
            .getResult()

    GitlabWebHookHelper.handleMultibranchWebHookForBranch(context)

    BaseProcess process = createProcess(context, branchName)
    if (process == null) {
        error "${branchName} 无任何相关联流水线实现，忽略..."
    } else {
        if (process.checkNeedBuild()) {
            process.handleProcess()
            process.checkProcessFailed()
        }

        echo "---------- 执行结束 ----------"
    }
}

