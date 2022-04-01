package pipeline.ugbasic

import entity.Context
import enums.DeployCluster

/**
 * @author lijq* @date 2022/3/18
 */
class UserProcess extends UgBasicBaseProcess {

    UserProcess(Context context, String deployEnv, DeployCluster deployCluster) {
        super(context, deployEnv, deployCluster)
    }

    @Override
    protected String[] skipSonarCheckProjects() {
        return [
                "h3yun-token-dao",
                "h3yun-user-captcha"
        ]
    }

    @Override
    protected String getDeployNamespace() {
        return "h3yun-user"
    }

    @Override
    protected careProjects() {
        return UgBasicProjectNames.UG_USER_PROJECT_NAMES
    }
}
