package pipeline.lateinos.runtime


import entity.Context
import enums.DeployCluster
import pipeline.lateinos.runtime.IntegrateBaseProcess

/**
 *  预发布流程处理
 */
class PrePublishProcess extends IntegrateBaseProcess{
    PrePublishProcess(Context context) {
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
        return "lateinos"
    }

    @Override
    protected String getTargetBranchNameAfterVerify() {
        return "main"
    }

}
