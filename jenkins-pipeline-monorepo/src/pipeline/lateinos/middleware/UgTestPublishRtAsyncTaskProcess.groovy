package pipeline.lateinos.middleware

import entity.Context
import enums.DeployCluster

/**
 *  UG测试集群默认发布
 */
class UgTestPublishRtAsyncTaskProcess extends InformalPublishRtAsyncTaskBaseProcess {
    UgTestPublishRtAsyncTaskProcess(Context context) {
        super(context)
    }

    @Override
    protected DeployCluster getDeployCluster() {
        return DeployCluster.UG_TEST
    }

    @Override
    protected String getVirtualEnv() {
        return null
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
    protected String getTargetBranchAfterVerify() {
        return "main"
    }
}
