package pipeline.ugengine

import cmd.DotnetPackageCmd
import entity.Context
import entity.ProjectItemConfig
import pipeline.BaseProcess
import pipeline.ugengine.sdk.BuildDotnetSDKCMD

import java.text.SimpleDateFormat

class DevSdkProcess extends BaseProcess {
    DevSdkProcess(Context context) {
        super(context)
    }

    @Override
    protected careProjects() {
        return [UgEngineProjectNames.ENGINE_SDK_INFRASTRUCTRUE,
        UgEngineProjectNames.ENGINE_SDK_ORM]
    }

    @Override
    protected void execute() {
        this.context.jenkins.stage('SDK编译') {
            ProjectItemConfig config_engine_infrastructure = this.context.projectsConfig.get(UgEngineProjectNames.ENGINE_SDK_INFRASTRUCTRUE)
            buildDotnetSdk(config_engine_infrastructure)

//            ProjectItemConfig config_org_sdk = this.context.projectsConfig.get(UgEngineProjectNames.ENGINE_SDK_ORG)
//            buildDotnetSdk(config_org_sdk)

            ProjectItemConfig config_orm_sdk = this.context.projectsConfig.get(UgEngineProjectNames.ENGINE_SDK_ORM)
            buildDotnetSdk(config_orm_sdk)
        }
    }

    private void buildDotnetSdk(ProjectItemConfig config_engine_sdk){
        if(!config_engine_sdk.needBuild) {
            return
        }
        BuildDotnetSDKCMD buildDotnetSDKCMD = new BuildDotnetSDKCMD(this.context)
        buildDotnetSDKCMD.setConfig_sdk(config_engine_sdk)
                        .setNeedPush(true)
                        .execute()
    }
}
