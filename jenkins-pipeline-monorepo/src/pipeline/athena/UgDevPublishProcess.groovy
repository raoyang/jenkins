package pipeline.athena

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

/**
 *  有格开发环境发布流程
 */
class UgDevPublishProcess extends UgEnvPublishProcess {
   UgDevPublishProcess(Context context) {
        super(context)
    }

    @Override
    protected DeployCluster getDeployCluster() {
        return DeployCluster.UG_DEV
    }

    @Override
    protected String getArgoDeployEnv() {
        return "dev"
    }

    @Override
    protected String getK8sNamespace() {
        return "athena"
    }

    @Override
    protected String getTargetBranchNameAfterVerify() {
        return "ug-test-publish"
    }
}
