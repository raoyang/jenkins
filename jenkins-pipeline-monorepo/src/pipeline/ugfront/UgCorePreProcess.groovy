package pipeline.ugfront

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import cmd.BuildNotifyCmd
import cmd.FrontAppPackageCmd
import cmd.FrontLcosKernelPackageCmd
import cmd.FrontPolymerPackageCmd
import cmd.InputMessageCmd
import cmd.OssUploadCmd
import entity.Context
import entity.ProjectItemConfig
import pipeline.BaseProcess

class UgCorePreProcess extends UgCoreBaseProcess {

    UgCorePreProcess(Context context) {
        super(context)
    }

    protected String getCloudDir(String module, String version) {
        String dir = "";
        switch(module) {
            case "pc":
                dir = "web-static-pre/ug-pc-app-monorepo";
                break;
            case "mobile":
                dir = "web-static-pre/ug-mobile-app-monorepo";
                break;
            case "external":
                dir = "web-static-pre/ug-external-app-monorepo";
                break;
            default:
                throw new IllegalArgumentException("错误的模块：" + module);
        }
        return dir + "/v" + version;
    }

    protected void doVerification(){
        if(manualVerification(Arrays.asList(this.context.globalConfig.teamLeader), false)) {
            //            // 发布到 npm仓库
            //            ProjectItemConfig lcosKernelConfig = projectsConfig.get(ProjectNames.LCOS_KERNEL);
            //            // kernel自身、PC端、移动端、外链 变更都要触发kernel打包
            //            if (lcosKernelConfig.needBuild
            //                    || pcPloymerConfig.needBuild
            //                    || mobilePloymerConfig.needBuild
            //                    || externalPloymerConfig.needBuild) {
            //                new FrontLcosKernelPackageCmd(this.context)
            //                        .setProjectItemConfig(lcosKernelConfig)
            //                        .setPublish(true)
            //                        .execute();
            //            }
            this.processMerge("pre", "main");
        }
    }
}
