package pipeline.athena

import entity.Context
import pipeline.BaseProcess

/**
 * 主分支流程
 */
class MainProcess extends BaseProcess {
    MainProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return null
    }

    @Override
    protected void execute() {
        /**
         *  将主分支，同步到所有的所有的开发类型分支
         */
        this.mergeToDev("main")

        /**
         *  处理特定分支合并
         */
    }
}
