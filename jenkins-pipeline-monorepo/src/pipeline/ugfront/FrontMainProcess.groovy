package pipeline.ugfront

import entity.Context
import pipeline.BaseProcess

class FrontMainProcess extends BaseProcess {

    FrontMainProcess(Context context) {
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