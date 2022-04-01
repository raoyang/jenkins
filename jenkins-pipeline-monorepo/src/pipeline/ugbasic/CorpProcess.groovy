package pipeline.ugbasic

import entity.Context
import enums.DeployCluster

/**
 * @author lijq* @date 2022/3/18
 */
class CorpProcess extends UgBasicBaseProcess {

    CorpProcess(Context context, String deployEnv, DeployCluster deployCluster) {
        super(context, deployEnv, deployCluster)
    }

    @Override
    protected String[] skipSonarCheckProjects() {
        return [
                "h3yun-org-dao"
        ]
    }

    @Override
    protected String getDeployNamespace() {
        return "h3yun-corp"
    }

    @Override
    protected careProjects() {
        return UgBasicProjectNames.UG_CORP_PROJECT_NAMES
    }
}
