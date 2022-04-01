import cmd.ContextInfoCmd
import entity.Context
import pipeline.BaseProcess
import pipeline.GitlabWebHookHelper
import pipeline.ugengine.DevAppIntegrationProcess
import pipeline.ugengine.DevAutoTestProcess
import pipeline.ugengine.DevEngineAdminProcess
import pipeline.ugengine.DevHaProcess
import pipeline.ugengine.DevMultiTenantProcess
import pipeline.ugengine.DevSdkProcess
import pipeline.ugengine.DevCleanerProcess
import pipeline.ugengine.DevCoreProcess
import pipeline.ugengine.DevWorkflowProcess
import pipeline.ugengine.MainProcess
import pipeline.ugengine.TestProcess
import pipeline.ugengine.DevDatachangedProcess
import pipeline.ugengine.DevStatisticsProcess
import pipeline.ugengine.PrePublishProcess

static def createProcess(Context context, String branchName) {
    switch (branchName) {
        case "dev-statistics":
            return new DevStatisticsProcess(context)
        case "dev-datachanged":
            return new DevDatachangedProcess(context)
        case "dev-cleaner":
            return new DevCleanerProcess(context)
        case "dev-core":
            return new DevCoreProcess(context)
        case "dev-workflow":
            return new DevWorkflowProcess(context)
        case "dev-sdk":
            return new DevSdkProcess(context)
        case "dev-ha":
            return new DevHaProcess(context)
        case "dev-multi-tenant":
            return new DevMultiTenantProcess(context)
        case "dev-autotest":
            return new DevAutoTestProcess(context)
        case "dev-app-integration":
            return new DevAppIntegrationProcess(context)
        case "dev-engine-admin":
            return new DevEngineAdminProcess(context)
        case "test":
            return new TestProcess(context)
        case "pre-publish":
            return new PrePublishProcess(context)
        case "main":
            return new MainProcess(context)
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
            .setFilePath("UgEngineProjectConfig.yaml")
            .setBranchName(branchName)
            .execute()
            .getResult()

    echo "开始处理多分支的webhook..."
    GitlabWebHookHelper.handleMultibranchWebHookForBranch(context)
    echo "结束处理多分支的webhook..."

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

