package pipeline.ugengine

import entity.Context
import pipeline.BaseProcess

/**
 *  Main分支流程流水线
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
        String[] devBranches = [
                "dev-app-integration",
                "dev-autotest",
                "dev-cleaner",
                "dev-core",
                "dev-datachanged",
                "dev-engine-admin",
                "dev-ha",
                "dev-multi-tenant",
                "dev-sdk",
                "dev-statistics",
                "dev-workflow",
                "test"
        ]
        for(String branch in devBranches){
            execMergeCmd('main', branch,"合并代码到${branch}")
        }
    }
}