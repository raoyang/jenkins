package pipeline.ugengine


import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

/**
 *  pre-publish分支流程流水线
 */
class PrePublishProcess extends BaseProcess {
    PrePublishProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return null
    }

    @Override
    protected void execute() {

    }
}