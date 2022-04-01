package pipeline.lateinos.runtime

import entity.Context
import enums.DeployCluster
import pipeline.lateinos.runtime.IntegrateBaseProcess

/**
 * 集成测试环节实现
 */
class TestProcess extends IntegrateBaseProcess{

    TestProcess(Context context) {
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
        return "lateinos"
    }

    @Override
    protected String getTargetBranchNameAfterVerify() {
        return "pre-publish"
    }

}
