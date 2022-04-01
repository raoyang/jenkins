package pipeline.athena

import entity.Context
import entity.ProjectItemConfig
import enums.DeployCluster
import pipeline.BaseProcess

/**
 *  上线流程
 */
class UgIdcPublishProcess extends UgEnvPublishProcess {
    UgIdcPublishProcess(Context context) {
        super(context)
    }

    @Override
    protected DeployCluster getDeployCluster() {
        return DeployCluster.UG_IDC
    }

    @Override
    protected String getArgoDeployEnv() {
        return "ug-idc"
    }

    @Override
    protected String getK8sNamespace() {
        return "athena"
    }

    @Override
    protected String getTargetBranchNameAfterVerify() {
        return "main"
    }
}
