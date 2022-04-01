package pipeline.athena

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

/**
 *  有格测试环境发布流程
 */
class UgTestPublishProcess extends UgEnvPublishProcess {
    UgTestPublishProcess(Context context) {
        super(context)
    }

    @Override
    protected DeployCluster getDeployCluster() {
        return DeployCluster.UG_TEST
    }

    @Override
    protected String getArgoDeployEnv() {
        return "test"
    }

    @Override
    protected String getK8sNamespace() {
        return "athena"
    }

    @Override
    protected String getTargetBranchNameAfterVerify() {
        return "ug-idc-publish"
    }
}
