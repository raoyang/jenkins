package pipeline.lateinos.middleware

import entity.Context
import enums.DeployCluster

/**
 *  有格开发集群默认发布
 */
class UgDevPublishRtAsyncTaskProcess extends InformalPublishRtAsyncTaskBaseProcess {
    protected UgDevPublishRtAsyncTaskProcess(Context context) {
        super(context)
    }

    @Override
    protected DeployCluster getDeployCluster() {
        return DeployCluster.UG_DEV
    }

    @Override
    protected String getVirtualEnv() {
        return null
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
    protected String getTargetBranchAfterVerify() {
        return "ug-test-publish-rt-asynctask"
    }
}
