import cmd.ContextInfoCmd
import entity.Context
import pipeline.BaseProcess
import pipeline.GitlabWebHookHelper
import pipeline.lateinos.middleware.DevXxlJobProcess
import pipeline.lateinos.middleware.UgDevPublishRtAsyncTaskProcess
import pipeline.lateinos.middleware.UgTestPublishRtAsyncTaskProcess
import pipeline.lateinos.runtime.DevApiServerProcess
import pipeline.lateinos.runtime.DevControllerAgentProcess
import pipeline.lateinos.runtime.DevGatewayProcess
import pipeline.lateinos.middleware.DevRtAsyncTaskProcess
import pipeline.lateinos.runtime.DevRuntimeServerProcess
import pipeline.lateinos.DevSdkProcess
import pipeline.lateinos.FunctionTestProcess
import pipeline.lateinos.MainProcess
import pipeline.lateinos.runtime.PrePublishProcess
import pipeline.lateinos.runtime.PublishProcess
import pipeline.lateinos.SdkPublishProcess
import pipeline.lateinos.runtime.TestProcess

static def createProcess(Context context, String branchName) {
    switch (branchName) {
        case "dev-controller-agent":
            return new DevControllerAgentProcess(context)
        case "dev-runtime-server":
            return new DevRuntimeServerProcess(context)
        case "dev-xxljob":
            return new DevXxlJobProcess(context)
        case "dev-rt-asynctask":
            return new DevRtAsyncTaskProcess(context)
        case "ug-dev-publish-rt-asynctask":
            return new UgDevPublishRtAsyncTaskProcess(context)
        case "ug-test-publish-rt-asynctask":
            return new UgTestPublishRtAsyncTaskProcess(context)
        case "dev-sdk":
            return new DevSdkProcess(context)
        case "dev-api-server":
            return new DevApiServerProcess(context)
        case "dev-gateway":
            return new DevGatewayProcess(context)
        case "function-test":
            return new FunctionTestProcess(context)
        case "test":
            return new TestProcess(context)
        case "pre-publish":
            return new PrePublishProcess(context)
        case "publish":
            return new PublishProcess(context)
        case "sdk-publish":
            return new SdkPublishProcess(context)
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
            .setFilePath("LateinosProjectConfig.yaml")
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

