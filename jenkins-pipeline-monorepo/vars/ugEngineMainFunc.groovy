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
    echo("??????????????????: ${branchName}")
    return branchName
}

def call() {
    echo "---------- ???????????? ----------"

    String workspace = env.WORKSPACE
    echo "??????????????????: ${workspace}"

    String branchName = getBranchName()
    if (branchName == null || branchName.isEmpty()) {
        // ?????????
        echo("??????????????????????????????????????????????????????!!!")
        return
    }

    sh("ls -l")
    echo sh(returnStdout: true, script: 'env')

    // ????????????
    Context context = new ContextInfoCmd(this)
            .setFilePath("UgEngineProjectConfig.yaml")
            .setBranchName(branchName)
            .execute()
            .getResult()

    echo "????????????????????????webhook..."
    GitlabWebHookHelper.handleMultibranchWebHookForBranch(context)
    echo "????????????????????????webhook..."

    BaseProcess process = createProcess(context, branchName)
    if (process == null) {
        error "${branchName} ??????????????????????????????????????????..."
    } else {
        if (process.checkNeedBuild()) {
            process.handleProcess()
            process.checkProcessFailed()
        }

        echo "---------- ???????????? ----------"
    }
}

