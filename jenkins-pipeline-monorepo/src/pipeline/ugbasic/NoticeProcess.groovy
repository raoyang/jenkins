package pipeline.ugbasic

import entity.Context
import enums.DeployCluster

/**
 * @author lijq* @date 2022/3/18
 */
class NoticeProcess extends UgBasicBaseProcess {

    NoticeProcess(Context context, String deployEnv, DeployCluster deployCluster) {
        super(context, deployEnv, deployCluster)
    }

    @Override
    protected String[] skipSonarCheckProjects() {
        return new String[0]
    }

    @Override
    protected String getDeployNamespace() {
        return "h3yun-notice"
    }

    @Override
    protected careProjects() {
        return UgBasicProjectNames.UG_NOTICE_PROJECT_NAMES
    }
}
