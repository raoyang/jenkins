import cmd.ContextInfoCmd
import cmd.InitGitCmd
import entity.Context
import pipeline.BaseProcess
import pipeline.guojh.GuojhProcess

def createProcess(Context context, String branchName) {
    switch (branchName) {
        case "dev-guojh":
            return new GuojhProcess(context)
    }
    return null
}

def call() {
    echo "---------- 构建开始 ----------"

    String workspace = env.WORKSPACE
    echo "工作空间目录: ${workspace}"

    String branchName = params.buildBranch
    // 解析配置
    echo "当前构建分支: ${branchName}"

    sh("ls -l")

    echo sh(returnStdout: true, script: 'env')

    Context context = new ContextInfoCmd(this)
            .setFilePath("LateinosProjectConfig.yaml")
            .execute()
            .getResult()

    new InitGitCmd(context).execute()

    BaseProcess process = createProcess(context, branchName)
    if (process == null) {
        echo "${branchName} 无任何相关联流水线实现，忽略..."
    } else {
        process.execute()
        echo "---------- 构建结束 ----------"
    }
}
